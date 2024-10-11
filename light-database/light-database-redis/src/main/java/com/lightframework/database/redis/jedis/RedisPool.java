package com.lightframework.database.redis.jedis;

import com.lightframework.database.redis.config.JedisConfig;
import com.lightframework.database.redis.queue.Handler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Jedis客户端工具类
 * 自带重连机制,尝试重连时间第一次间隔1秒,之后每次失败间隔时间扩大二倍,直到间隔时间最大固定为2分钟
 * @author ：mashuai
 * @create 2022-05-06 10:03
 **/
@Slf4j
@Data
public class RedisPool {

    private String redisName;//redis连接名称
    private JedisConfig.RedisConfig config = null;//redis配置信息
    private JedisPoolConfig poolConfig = null;//连接池配置信息

    private JedisPool pool = null;
    private Timer reconnectTimer = null;//重连定时器
    private volatile Boolean reconnecting = false;//重连机制正在工作中
    private final Integer MAXDELAY = 128_000;//单位毫秒 重连间隔时间 最大值 2分8秒钟
    //实际redis连接失败时: 真实间隔时间 =  redis.yml的 connectionTimeout连接超时时间 + reconnectDelay
    private Integer reconnectDelay = 1000;//单位毫秒 重连间隔时间 起始值,从起始值开始尝试重连,每次重连失败间隔时间扩大二倍,直到间隔最大两分钟.

    //redis重连机制下的key监听支持
    private String[] keys = null;
    private Handler handler;//redis的Key监控处理

    private RedisPool() {
    }
    public RedisPool(String redisName, JedisConfig.RedisConfig redisConfig, JedisPoolConfig poolConfig) {
        this.redisName = redisName;
        this.config = redisConfig;
        this.poolConfig = poolConfig;
        //初始化当前连接池
        if( !initPool() ){//初始化连接池,并判断失败时
            startReconnectCycle();//启动重连机制
        }
        //JVM退出钩子函数,监听JVM退出
        Runtime.getRuntime().addShutdownHook(new Thread("JVM钩子"){
            @Override
            public void run() {
                log.info("RedisPool JVM销毁钩子触发销毁连接池 JedisPool [{}]",redisName);
                if( pool != null && !pool.isClosed() ){
                    pool.close();//关闭连接池
                }
            }
        });//JVM退出钩子函数,监听JVM退出
    }

    /**
     * 获取连接池中的连接
     */
    public Jedis getConnection(){
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
        } catch (JedisConnectionException e) {
            //log.info("RedisPool [{}] 获取连接失败,准备启动自动重连...", redisName, e.getMessage());
            if( !reconnecting ){//不是正在重连中...
                startReconnectCycle();//启动重连
            }
        } catch (Exception e) {
            log.error("从连接池 ["+redisName+"] 中获取redis连接失败", e);
        }
        return jedis;
    }



    /**
     * 初始化Jedis连接池
     */
    private synchronized boolean initPool() {
        Boolean success = false;//默认失败
        try {
            //构造连接池
            pool = new JedisPool(poolConfig, config.getHost(), config.getPort(), config.getConnectionTimeout(), config.getSoTimeout(), config.getPassword(), config.getDatabase(), this.redisName);
            pool.getResource().close();//获取一个连接并归还连接池,测试连接是否可用
            success = true;//初始化连接池成功
        }catch (JedisConnectionException e){
            log.error("RedisPool [{}] 初始化失败,无法连接Redis", redisName);
        } catch (Exception e) {
            log.error("RedisPool 初始化异常 ["+redisName+"] ", e);
        }
        return success;
    }

    /**
     * 重新连接
     */
    private void reconnect(){
        boolean suc = initPool();//尝试重连
        //log.info("RedisPool [{}] 尝试重连 success={}", redisName, suc);
        if( suc ){
            onSuccess();//连接成功
        }else {
            onFailure();//连接失败
        }
    }

    /**
     * 重连任务
     */
    private TimerTask reconnectTask(){
        return new TimerTask() {
            @Override
           public  void run() {
                try {
                    log.info("RedisPool [{}] 重连任务 执行...", redisName);
                    reconnect();//重连任务
                } catch (Exception e) {
                    log.error("RedisPool ["+redisName+"] 重连任务 执行异常", e);
                }
            }
        };
    }


    /**
     * 启动自动重连机制
     */
    public synchronized void startReconnectCycle() {
        try {
            if( !reconnecting ){//不是正在重连中
                reconnecting = true;//正在重连中
                log.info("RedisPool [{}] 启动重连机制", redisName);
                reconnectTimer = new Timer("Timer-redis重连机制");
                reconnectTimer.schedule(reconnectTask(), reconnectDelay);//调度执行一次
            }
        } catch (Exception e) {
            log.error("RedisPool ["+redisName+"] 启动重连机制 异常", e);
        }

    }

    /**
     * 停止自动重连机制
     */
    private synchronized void stopReconnectCycle() {
            if ( reconnecting ) {//正在重连
                if (reconnectTimer != null) {//定时器不为空
                    reconnectTimer.cancel();//停止定时器
                    reconnectTimer = null;
                }
                reconnectDelay = 1000; // Reset Delay Timer
            }
            reconnecting = false;

    }

    /**
     * 重连成功
     */
    private void onSuccess() {
        stopReconnectCycle();//停止重连
        //redis重连成功时需要重新监听key
        log.info("RedisPool [{}] 重连成功,重新监听keys={} handler={}", redisName, keys, handler);
        if( keys != null ){
            JedisUtil.use(redisName).spaceNotifyWithNoReconnect(handler,keys);
        }
    }

    /**
     * 重连失败
     */
    private void onFailure() {
        //从起始值开始尝试重连,每次重连失败间隔时间扩大二倍,直到间隔两分钟为止.
        if ( reconnectDelay < MAXDELAY ) {//2分8秒钟
            reconnectDelay = reconnectDelay * 2;
        }
        rescheduleReconnectCycle();//重启调度
    }

    /**
     * 再次重连
     */
    private synchronized void rescheduleReconnectCycle() {
            if ( reconnecting ) {//正在重连
                if (reconnectTimer != null) {//重连定时器不为空
                    reconnectTimer.schedule(reconnectTask(), reconnectDelay);//调度执行一次
                } else {
                    // The previous reconnect timer was cancelled
                    startReconnectCycle();
                }
            }

    }



    /**
     * 销毁连接实例
     * @param jedis
     */
   public void close(Jedis jedis){
        try {
            if (jedis != null) {
                jedis.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("关闭jedis实例异常！", e);
        }
    }

    /**
     * 销毁连接池中的所有连接
     */
   public void destroy(){
        if (pool != null) {
            pool.destroy();
        }
    }
}

