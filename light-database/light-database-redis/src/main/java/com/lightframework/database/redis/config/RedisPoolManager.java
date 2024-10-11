package com.lightframework.database.redis.config;

import com.lightframework.database.redis.jedis.JedisUtil;
import com.lightframework.database.redis.jedis.RedisPool;
import com.lightframework.database.redis.queue.QueueUtil;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** redis 连接池管理者
 * @author ：mashuai
 * @date ：2022/08/14 18:36
 */
@Slf4j
public class RedisPoolManager {

    private static String primary;//默认优先使用的redis连接
    //RedisPool 多数据源
    private static final ConcurrentHashMap<String, RedisPool> poolMap = new ConcurrentHashMap<>();//多数据源 连接池集合 <redis名称,RedisPool>
    //JedisUtil 多数据源
    private static final ConcurrentHashMap<String, JedisUtil> jedisUtilMap = new ConcurrentHashMap<>();//多数据源 连接池集合 <redis名称,JedisUtil>
    //QueueUtil 多数据源
    private static final ConcurrentHashMap<String, QueueUtil> queueUtilMap = new ConcurrentHashMap<>();//多数据源 连接池集合 <redis名称,QueueUtil>

    static {//静态块 初始化 多数据源
        JedisConfig jedisConfig = JedisConfig.getInstance();//Redis配置文件 redis.yml
        primary = jedisConfig.getPrimary();//默认优先使用的redis连接
        Map<String, JedisConfig.RedisConfig> redisPoolMap = jedisConfig.getRedisPool();//redis连接客户端集合
        //迭代初始化 多数据源
        for (Map.Entry<String, JedisConfig.RedisConfig> entry : redisPoolMap.entrySet()) {//迭代多数据源
            JedisConfig.RedisConfig redisConfig = entry.getValue();//redis数据源
            //连接池配置
            JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
            jedisPoolConfig.setMaxIdle(redisConfig.getMaxIdle());
            jedisPoolConfig.setMaxTotal(redisConfig.getMaxTotal());
            jedisPoolConfig.setMaxWaitMillis(redisConfig.getMaxWaitMillis());
            jedisPoolConfig.setMinEvictableIdleTimeMillis(redisConfig.getMinEvictableIdleTimeMillis());
            jedisPoolConfig.setNumTestsPerEvictionRun(redisConfig.getNumTestsPerEvictionRun());
            jedisPoolConfig.setTestWhileIdle(redisConfig.getTestWhileIdle());
            jedisPoolConfig.setTestOnCreate(redisConfig.getTestOnCreate());
            jedisPoolConfig.setTestOnBorrow(redisConfig.getTestOnBorrow());
            //构造 RedisPool客户端工具
            RedisPool redisPool = new RedisPool(entry.getKey(), redisConfig, jedisPoolConfig);//redis连接客户端
            poolMap.put(redisPool.getRedisName(),redisPool);//存入 连接池集合 <redis名称,redis连接客户端>
            //存入 JedisUtil 客户端
            jedisUtilMap.put(entry.getKey(),new JedisUtil(redisPool));//存入 Jedis 工具
            //存入 QueueUtil 客户端
            queueUtilMap.put(entry.getKey(),new QueueUtil(entry.getKey(), redisPool));//存入 Jedis 工具
        }

    }

    /**
     * 获取连接池中的连接,默认首选连接池
     */
    public static Jedis getConnection(){
        Jedis jedis = null;
        try {
            RedisPool redisPool = poolMap.get(primary);//redis连接客户端
            jedis = redisPool.getConnection();
        } catch (Exception e) {
            log.error("RedisPoolManager 获取默认首选连接池中的连接失败", e);
        }
        return jedis;
    }
    /**
     * 获取连接池中的连接,默认首选连接池
     */
    public static Jedis getConnection(String redisName){
        Jedis jedis = null;
        try {
            RedisPool redisPool = poolMap.get(redisName);//redis连接客户端
            jedis = redisPool.getConnection();
        } catch (Exception e) {
            log.error("RedisPoolManager 获取 "+redisName+" 中的连接失败", e);
        }
        return jedis;
    }

    /**
     * 获取 JedisUtil
     */
    public static JedisUtil getJedisUtil(){
        return jedisUtilMap.get(primary);
    }

    /**
     * 获取 JedisUtil
     */
    public static JedisUtil getJedisUtil(String redisName){
        return jedisUtilMap.get(redisName);
    }
    /**
     * 获取 QueueUtil
     */
    public static QueueUtil getQueueUtil(){
        return queueUtilMap.get(primary);
    }

    /**
     * 获取 QueueUtil
     */
    public static QueueUtil getQueueUtil(String redisName){
        return queueUtilMap.get(redisName);
    }







    /*
    JedisUtil 支持多数据源 设计思路
    1. JedisUtil 多例模式, 每个实例都使用自己的数据源, 实例个数对应配置文件中的连接个数
    2. JedisUtil 调用方法使用 use(redis名称) 来区别数据源使用, JedisUtil.user().默认使用primary首选数据源 ; JedisUtil.use(redis名称). 使用指定数据源
     */


}
