package com.lightframework.database.redis.jedis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lightframework.database.redis.queue.Handler;
import com.lightframework.database.redis.config.JedisConfig;
import com.lightframework.database.redis.config.RedisPoolManager;
import com.lightframework.database.redis.queue.RedisKeyListener;
import com.lightframework.database.redis.util.JacksonUtil;
import com.lightframework.util.serialize.SerializeUtil;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.params.SortingParams;
import redis.clients.jedis.params.XReadGroupParams;
import redis.clients.jedis.params.XReadParams;
import redis.clients.jedis.resps.StreamEntry;
import redis.clients.jedis.resps.Tuple;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @desc Jedis客户端工具类
 * @create 2022-05-06 10:03
 **/
@Slf4j
public class JedisUtil {

//    private String redisName;//redis连接名称
    private RedisPool redisPool;//Jedis客户端工具

    /**
     * 使用此方法记得用完调用 {@link RedisPool#close(Jedis)}
     * @return
     */
    public RedisPool getRedisPool(){
        return redisPool;
    }

    private JedisUtil() {}//私有化构造,不允许new对象

    public JedisUtil(RedisPool redisPool) {
        this.redisPool = redisPool;
    }

    /**
     * 默认使用 首选数据源
     * @return redis连接
     */
    public static JedisUtil use(){
        return RedisPoolManager.getJedisUtil();
    }
    /**
     * 根据数据源 名称
     * @return redis连接
     */
    public static JedisUtil use(String redisName){
        return RedisPoolManager.getJedisUtil(redisName);
    }

    private static RedisKeyListener redisKeyListener = new RedisKeyListener();

    /**
     * 提醒: 请使用一个单独的redis连接来执行阻塞的key监听,这样可以更好的支持自动重连redis之后key监听不会失效
     * 支持自动重连的key监听(redis重启后也不会丢失key监听)
     * 监听 键空间通知（key-space notification）
     *  键空间通知通常是不启用的，因为这个过程会产生额外消耗。 所以在使用该特性之前，请确认一定是要用这个特性的，然后修改配置文件。
     * 配置文件中notify-keyspace-events "AKE" 修改为这个即可生效
     * @param handler
     * @param keys
     */
    public void spaceNotify(Handler handler,String ...keys) {
        //redis重连机制下的key监听支持
        setSpaceNotifyConfig(handler,keys);
        //redis添加key监听
        spaceNotifyWithNoReconnect(handler,keys);
    }

    /**
     * 不支持自动重连的key监听(redis重启时会丢失key监听)
     * @param handler
     * @param keys
     */
    public void spaceNotifyWithNoReconnect(Handler handler,String ...keys) {
        //redis添加key监听
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            redisKeyListener.setHandler(handler);
            jedis.psubscribe(redisKeyListener,keyspace(keys));
        }catch (JedisConnectionException connectionException){
            log.info("RedisPool [{}] key监听 连接失败,准备启动自动重连...", redisPool.getRedisName(), connectionException);
            if( !redisPool.getReconnecting() ){//不是正在重连中...
                redisPool.startReconnectCycle();//启动重连
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + Arrays.asList(keys), e);
        }finally {
            redisPool.close(jedis);
        }
    }
    private String[] keyspace(String[] keys){
        JedisConfig.RedisConfig config = redisPool.getConfig();//当前连接的库名
        String[] arr = new String[keys.length];
        for(int i=0;i<keys.length;i++){
            arr[i] = "__keyspace@" + config.getDatabase() + "__:"+keys[i];
        }
        log.info("Redis键监听 {} {} {} {} 监听空间 {}", redisPool.getRedisName(), config.getHost(), config.getPort(), config.getDatabase(), Arrays.asList(arr));
        return arr;
    }
    /**
     * 设置key监听的配置
     * @param handler
     * @param keys
     */
    public void setSpaceNotifyConfig(Handler handler,String ...keys) {
        //redis重连机制下的key监听支持
        redisPool.setKeys(keys);
        redisPool.setHandler(handler);
    }


    /**
     * 监听 键事件通知（key-event notification）
     *  配置文件中修改  notify-keyspace-events “Kx”，注意：这个双引号是一定要的，否则配置不成功，启动也不报错。
     * @param event
     */
    public void eventNotify(String ...event) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            String[] arr = new String[event.length];
            for(int i=0;i<event.length;i++){
                arr[i] = "__keyevent@*__:"+event[i];
            }
            jedis.psubscribe(redisKeyListener,arr);
        }catch (Exception e){
            log.error("Jedis操作数据 异常", e);
        }finally {
            redisPool.close(jedis);
        }
    }

    /**
     * 清空redis数据库,慎用!
     */
    public String flushDB() {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        String response = "";
        try{
            response = jedis.flushDB();
        }catch (Exception e){
            log.error("Jedis操作数据 异常", e);
        }finally {
            redisPool.close(jedis);
        }
        return response;
    }

    /**
     * 判断某个键是否存在
     * @param key
     * @return
     */
    public boolean exists(String key) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        Boolean res = false;
        try{
            if( jedis != null ){
                res = jedis.exists(key);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 新增/覆盖数据项
     * @param key
     * @param value
     * @return 执行成功返回OK
     */
    public String set(String key, String value) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        String res = "fail";
        try{
            if( jedis != null ){
                res = jedis.set(key, value);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , "+value, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }
    /**
     * 新增/覆盖数据项
     * @param key
     * @param list
     * 对应 {@link JedisUtil#getTypeReference(String, com.fasterxml.jackson.core.type.TypeReference)}
     * @return 执行成功返回OK
     */
    public String setTypeReference(String key, Object list) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        String res = "fail";
        try{
            if( jedis != null ){
                res = jedis.set(key.getBytes(StandardCharsets.UTF_8), JacksonUtil.serialize(list));
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + list, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }
    /**
     * 新增/覆盖数据项
     * @param key
     * @param value
     * @params  seconds 有效秒数 当seconds>0时才会设置有效秒数,当seconds<=0时永久有效 例如0,-1都表示永久有效
     * @return 执行成功返回OK
     */
    public String set(String key, String value,final long seconds) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        String res = "fail";
        try{
            if( jedis != null ){
                res = jedis.set(key, value);
                if( "OK".equalsIgnoreCase(res) && seconds >0 ){//存入成功且需要设置有效秒数
                    jedis.expire(key, seconds);//有效秒数
                }
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + value, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 原子操作, key不存在则设置成功返回1, key已经存在则设置失败则返回0
     * 不覆盖增加数据项（key重复则不插入）
     * 返回 1已存在, 0不存在, -1异常
     * @param key
     * @param value
     * @return   1 if the key was set, 0 if the key was not set
     */
    public Long setnx(String key, String value) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        Long res = -1L;
        try{
            if( jedis != null ){
                res = jedis.setnx(key, value);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + value, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }
    /**
     * 原子操作, key不存在则设置成功返回1, key已经存在则设置失败则返回0
     * 不覆盖增加数据项（key重复则不插入）
     * 返回 1已存在, 0不存在, -1异常
     * @param key
     * @param value
     * @param second
     * @return   1 if the key was set, 0 if the key was not set
     */
    public Long setnx(String key, String value, Long second) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        Long res = -1L;
        try{
            if( jedis != null ){
                res = jedis.setnx(key, value);
                if( res == 1L ){//set成功时才设置过期时间
                    jedis.expire(key, second);
                }
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + value, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 增加数据项并设置有效时间（秒）
     * @param key
     * @param seconds 有效时间，单位：秒
     * @param value
     * @return
     */
    public String setex(String key, int seconds, String value) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        String res = "fail";
        try{
            if( jedis != null ){
                res = jedis.setex(key, seconds, value);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + value, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 增加多个键值对
     * @param var1，格式为 String k1,String v1,String k2,String v2,...
     * @return
     */
    public String mset(String... var1) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        String res = "fail";
        try{
            if( jedis != null ){
                res = jedis.mset(var1);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常", e);
        }finally {
            redisPool.close(jedis);
        }
        return  res;
    }

    /**
     * 获取多个key对应value
     * @param var1，格式为 String k1,String v1,String k2,String v2,...
     * @return
     */
    public List<String> mget(String... var1) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        List<String> list = null;
        try{
            if( jedis != null ){
                list = jedis.mget(var1);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常", e);
        }finally {
            redisPool.close(jedis);
        }
        return list;
    }

    /**
     * 获取所有匹配的key
     * @param pattern 正则模式
     * @return
     */
    public Set<String> keys(String pattern) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        Set<String> set = Collections.emptySet();
        try{
            if( jedis != null ){
                set = jedis.keys(pattern);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + pattern, e);
        }finally {
            redisPool.close(jedis);
        }
        return set;
    }

    /**
     * 删除键为key的数据项
     * @param key
     * @return An integer greater than 0 if one or more keys were removed, 0 if none of the specified keys existed
     */
    public Long del(String key) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        Long res = -1L;
        try{
            if( jedis != null ){
                res = jedis.del(key);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 删除多个key对应的数据项
     * @param keyArray
     * @return An integer greater than 0 if one or more keys were removed, 0 if none of the specified keys existed
     */
    public Long del(String[] keyArray) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        Long res = -1L;
        try{
            if( jedis != null ){
                res = jedis.del(keyArray);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + Arrays.asList(keyArray), e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 设置键为key数据项的过期时间为seconds秒
     * @param key
     * @return
     */
    public Long expire(String key, int seconds) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        Long res = -1L;
        try{
            if( jedis != null ){
                res = jedis.expire(key, seconds);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }
    /**
     * 设置键为key数据项的过期时间为seconds秒
     * @param key
     * @return
     */
    public Long expire(byte[] key, int seconds) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        Long res = -1L;
        try{
            if( jedis != null ){
                res = jedis.expire(key, seconds);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常", e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 获取键为key数据项的剩余生存时间（秒）
     * @param key
     * @return
     */
    public Long ttl(String key) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        Long res = -1L;
        try{
            if( jedis != null ){
                res = jedis.ttl(key);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 移除键为key数据项的生存时间限制
     * @param key
     * @return
     */
    public Long persist(String key) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        Long res = -1L;
        try{
            if( jedis != null ){
                res = jedis.persist(key);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 查看键为key对应value的数据类型
     * @param key
     * @return "none" if the key does not exist, "string" if the key contains a String value, "list"
     *    if the key contains a List value, "set" if the key contains a Set value, "zset" if the key
     *    contains a Sorted Set value, "hash" if the key contains a Hash value
     */
    public String type(String key) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        String res = "";
        try{
            if( jedis != null ){
                res = jedis.type(key);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 获取键为key对应的value
     * @param key
     * @return
     */
    public String get(String key) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        String res = "";
        try{
            if( jedis != null ){
                res = jedis.get(key);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 获取键为key对应的value
     * @param key
     * @param typeReference   泛型类型,例如 new TypeReference<List<HAlarm>>(){};     new TypeReference<Map<String, Object>>(){}
     * 对应 {@link JedisUtil#setTypeReference(String, Object)}
     * @return
     */
    public <T> T getTypeReference(String key, TypeReference<T> typeReference) {
        T list = null;
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            if( jedis != null ){
                byte[] bytes = jedis.get(key.getBytes(StandardCharsets.UTF_8));
                if( bytes != null && bytes.length > 0 ) list = JacksonUtil.deserialize(bytes, typeReference);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key, e);
        }finally {
            redisPool.close(jedis);
        }
        return list;
    }

    /**
     * 获取key对应value并更新value
     * @param key
     * @param value
     * @return
     */
    public String getSet(String key, String value) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        String res = "";
        try{
            if( jedis != null ){
                res = jedis.getSet(key, value);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + value, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 获取key对应value下标start到end之间的字符，包括end指向的字符
     * @param key
     * @param start
     * @param end
     * @return
     */
    public String getrange(String key, int start, int end) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        String res = "";
        try{
            if( jedis != null ){
                res = jedis.getrange(key, start, end);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 在key对应value后面扩展字符串appendStr
     * @param key
     * @param appendStr
     * @return 扩展后的value长度
     */
    public Long append(String key, String appendStr) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        Long res = -1L;
        try{
            if( jedis != null ){
                res = jedis.append(key, appendStr);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + appendStr, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 将key对应的value自增1
     * @param key
     * @return
     */
    public Long incr(String key) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        Long res = -1L;
        try{
            if( jedis != null ){
                res = jedis.incr(key);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 将key对应的value自增step
     * @param key
     * @param step 自增/减步长，整数
     * @return
     */
    public Long incrBy(String key, int step) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        Long res = -1L;
        try{
            if( jedis != null ){
                res = jedis.incrBy(key, step);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + step, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 将key对应的value自减1
     * @param key
     * @return
     */
    public Long decr(String key) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        Long res = -1L;
        try{
            if( jedis != null ){
                res = jedis.decr(key);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 将key对应的value自减step
     * @param key
     * @param step 自减/增步长，整数
     * @return
     */
    public Long decrBy(String key, int step) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        Long res = -1L;
        try{
            if( jedis != null ){
                res = jedis.decrBy(key, step);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + step, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     *
     * 列表（List）操作
     *
     */

    /**
     * 添加一个List
     * @param key
     * @param vals
     * @return
     */
    public Long lpush(String key, String[] vals) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        Long res = -1L;
        try{
            if( jedis != null ){
                res = jedis.lpush(key, vals);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + Arrays.asList(vals), e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 往key对应List左侧插入一个元素val
     * @param key
     * @param val
     * @return
     */
    public Long lpush(String key, String val) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        Long res = -1L;
        try{
            if( jedis != null ){
                res = jedis.lpush(key, val);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + val, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }
    public Long lpush(String key, Object object){
        byte[] channel = key.getBytes(StandardCharsets.UTF_8);
        //1、Protostuff序列化方式
        //byte[] val = SerializeUtil.serizlize(object);
        //2、目前采用Jackson2的序列化方式
        byte[] val = JacksonUtil.serialize(object);
        return lpush(channel, val);
    }
    /**
     * 将值插入队列头部,并校验队列最大大小,超过最大长度丢弃掉最老的消息
     * @param key 键
     * @param value 最新值
     * @param maxsize 允许存入的最大大小
     */
    public void lpushCheckLlen(String key, Object value, Long maxsize) {
        try {
            Long llen = JedisUtil.use().llen(key);
            if( llen < maxsize ){//未超最大限制
                lpush(key, value);//存入队列头部
            }else{//超过最大限制,移除尾部最老的消息,再存入最新的
                replace(key, value);
            }
        } catch (Exception e) {
            log.error("RedisUtil lpushCheckLlen方法异常 " + key + " , " + value, e);
        }
    }
    public void replace(String key,Object object){
        //超过最大限制,移除尾部最老的消息,再存入最新的
        rDelpop(key);//移除尾部一条消息
        lpush(key, object);//插入头部一条消息
    }
    /**
     * 往key对应List左侧插入一个元素val 对象
     * @param key
     * @param val
     * @return
     */
    public Long lpush(byte[] key, byte[] val) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        Long res = -1L;
        try{
            if( jedis != null ){
                res = jedis.lpush(key,val);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常", e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }
    /**
     * 往key对应List右侧插入一个元素val 对象
     * @param key
     * @param val
     * @return
     */
    public Long rpush(byte[] key, byte[] val) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        Long res = -1L;
        try{
            if( jedis != null ){
                res = jedis.rpush(key,val);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常", e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }
    public Long rpush(String key, Object object){
        byte[] channel = key.getBytes(StandardCharsets.UTF_8);
        //1、Protostuff序列化方式
        //byte[] val = SerializeUtil.serizlize(object);
        //2、目前采用Jackson2的序列化方式
        byte[] val = JacksonUtil.serialize(object);
        return rpush(channel, val);
    }

    /**
     * 获取key对应List区间[start, end]的元素，包括end下标指向的元素
     * @param key
     * @param start 开始下标
     * @param end   结束下标
     * @return
     */
    public List<String> lrange(String key, int start, int end) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        List<String> res = null;
        try{
            if( jedis != null ){
                res = jedis.lrange(key, start, end);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }
    /**
     * 这种方法不会影响 redis list 中的数据。
     * 获取key对应List区间[start, end]的元素，包括end下标指向的元素
     * @param key
     * @param start 开始下标
     * @param end   结束下标
     * @return
     */
    public <T>List<T> lrange(String key, int start, int end,Class<T> clazz) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        List<T> res = null;
        try{
            if( jedis != null ){
                byte[] byteKey = key.getBytes();
                List<byte[]> bytesList = jedis.lrange(byteKey, start, end);
                if( bytesList != null && bytesList .size() > 0 ){
                    for (int i = 0; i < bytesList.size(); i++) {
                        res.add(JacksonUtil.deserialize(bytesList.get(i), clazz));
                    }
                }
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + clazz, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 删除指定元素val个数num
     * @param key
     * @param num
     * @param val
     * @return
     */
    public Long lrem(String key, int num, String val) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        Long res = -1L;
        try{
            if( jedis != null ){
                res = jedis.lrem(key, num, val);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 删除list区间[start, end]之外的元素，end下标的元素被保留
     * @param key
     * @param start
     * @param end
     * @return OK：成功
     */
    public String ltrim(String key, int start, int end) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        String res = "";
        try{
            if( jedis != null ){
                res = jedis.ltrim(key, start, end);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * key对应list左端出栈一个元素
     * @param key
     * @return
     */
    public String lpop(String key) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        String res = "";
        try{
            if( jedis != null ){
                res = jedis.lpop(key);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * key对应list右端插入一个val
     * @param key
     * @param val
     * @return
     */
    public Long rpush(String key, String val) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        Long res = -1L;
        try{
            if( jedis != null ){
                res = jedis.rpush(key, val);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + val, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * key对应list右端出栈一个元素
     * @param key
     * @return
     */
    public String rpop(String key) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        String res = "";
        try{
            if( jedis != null ){
                res = jedis.rpop(key);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }
    public byte[] rpop(byte[] key) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        byte[] bytes = null;
        try{
            if( jedis != null ){
                bytes = jedis.rpop(key);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常", e);
        }finally {
            redisPool.close(jedis);
        }
        return bytes;
    }

    /**
     * 订阅消息 (非阻塞模式)
     * redis的 list队列
     * @param key 频道/主题
     * @param clazz 对象类型
     * @return 对象消息
     */
    public <T>T rpop(String key,Class<T> clazz) {
        byte[] byteKey = key.getBytes(StandardCharsets.UTF_8);
        byte[] bytes = this.rpop(byteKey);
        if( bytes != null ){
            return JacksonUtil.deserialize(bytes, clazz);
        }
        return null;
    }
    /**
     * 订阅消息 (非阻塞模式)
     * redis的 list队列
     * @param key 频道/主题
     * @param typeReference  泛型类型,例如 new TypeReference<List<RAlarm>>(){};     new TypeReference<Map<String, Object>>(){}
     * @return 对象消息
     */
    public <T>List<T> rpop(String key,TypeReference<List<T>> typeReference) {
        byte[] byteKey = key.getBytes(StandardCharsets.UTF_8);
        byte[] bytes = this.rpop(byteKey);
        if( bytes != null ){
            return JacksonUtil.deserialize(bytes, typeReference);
        }
        return null;
    }

    /**
     * 移除列表的最后一个元素,无返回值
     * @param key
     * @return
     */
    public void rDelpop(String key) {
        byte[] byteKey = key.getBytes(StandardCharsets.UTF_8);
        this.rpop(byteKey);
    }
    /**
     * key对应list右端出栈一个元素 阻塞模式(队列为空时该方法阻塞等待)
     * @param key
     * @return
     */
    public List<String> brpop(String key) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        List<String> list = null;
        try{
            if( jedis != null ){
                int timeout = 0;//阻塞等待超时时间毫秒数,0无限等待
                list = jedis.brpop(timeout,key);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key, e);
        }finally {
            redisPool.close(jedis);
        }
        return list;
    }
    public List<String> brpop(String key, int timeout) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        List<String> list = null;
        try{
            if( jedis != null ){
                //int timeout = 0;//阻塞等待超时时间毫秒数,0无限等待
                list = jedis.brpop(timeout,key);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + timeout, e);
        }finally {
            redisPool.close(jedis);
        }
        return list;
    }

    /**
     * 从 source队列 移动到 destination 队列
     * 阻塞模式(队列为空时该方法阻塞等待)
     * @param source 源队列
     * @param destination 目标队列
     * @param timeout 阻塞等待超时时间
     * @return
     */
    public String brpoplpush(String source, String destination, int timeout) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        String res = "";
        try{
            if( jedis != null ){
                res = jedis.brpoplpush(source,destination,timeout);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + source + " , " + destination, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }
    /**
     * key对应list右端出栈一个元素 阻塞模式(队列为空时该方法阻塞等待)
     * @param key
     * @return
     */
    public List<byte[]> brpop(byte[] key) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        List<byte[]> res = null;
        try{
            if( jedis != null ){
                int timeout = 0;//阻塞等待超时时间毫秒数,0无限等待
                res = jedis.brpop(timeout,key);
            }
        }catch (JedisConnectionException e){
            log.warn("redis连接[{}]断开", redisPool.getRedisName());
        }catch (Exception e){
            log.error("Jedis操作数据 异常", e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * key对应list右端出栈一个元素 阻塞模式(队列为空时该方法阻塞等待)
     * timeout设置超时时间 单位秒，如果超时时间内列表没有收到信息（无论是真的列表一直没有数据，还是连接池断开了），都会返回 null
     * @param key
     * @param timeout 阻塞等待超时时间毫秒数,0无限等待
     * @return
     */
    public List<byte[]> brpop(byte[] key, int timeout) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        List<byte[]> res = null;
        try{
            if( jedis != null ){
                res = jedis.brpop(timeout,key);
            }
        }catch (JedisConnectionException e){
            log.warn("redis连接[{}]断开", redisPool.getRedisName());
        }catch (Exception e){
            log.error("Jedis操作数据 异常", e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }
    /**
     * 订阅消息 (阻塞模式)
     * redis的 list队列简单模式
     * 阻塞模式(队列为空时该方法阻塞等待)
     * @param key 频道/主题
     * @param clazz 对象类型
     * @return 对象消息
     */
    public <T>T brpop(String key,Class<T> clazz) {
        byte[] byteKey = key.getBytes(StandardCharsets.UTF_8);
        List<byte[]> list = this.brpop(byteKey);
        if( list != null && list.size() == 2 ){
            // new String(list.get(0))) 就是 key
            return JacksonUtil.deserialize(list.get(1), clazz);
        }
        return null;
    }
    public <T>T brpop(String key,Class<T> clazz, int timeout) {
        byte[] byteKey = key.getBytes(StandardCharsets.UTF_8);
        List<byte[]> list = this.brpop(byteKey, timeout);
        if( list != null && list.size() == 2 ){
            // new String(list.get(0))) 就是 key
            return JacksonUtil.deserialize(list.get(1), clazz);
        }
        return null;
    }
    /**
     * 订阅消息 (阻塞模式)
     * redis的 list队列简单模式
     * 阻塞模式(队列为空时该方法阻塞等待)
     * @param key 频道/主题
     * @param typeReference  泛型类型,例如 new TypeReference<List<RAlarm>>(){};     new TypeReference<Map<String, Object>>(){}
     * @return 对象消息
     */
    public <T>List<T> brpop(String key,TypeReference<List<T>> typeReference) {
        byte[] byteKey = key.getBytes(StandardCharsets.UTF_8);
        List<byte[]> list = this.brpop(byteKey);
        if( list != null && list.size() == 2 ){
            // new String(list.get(0))) 就是 key
            return JacksonUtil.deserialize(list.get(1), typeReference);
        }
        return null;
    }
    public <T>List<T> brpop(String key,TypeReference<List<T>> typeReference, int timeout) {
        byte[] byteKey = key.getBytes(StandardCharsets.UTF_8);
        List<byte[]> list = this.brpop(byteKey, timeout);
        if( list != null && list.size() == 2 ){
            // new String(list.get(0))) 就是 key
            return JacksonUtil.deserialize(list.get(1), typeReference);
        }
        return null;
    }
    /**
     * 从 source队列 移动到 destination 队列
     * 阻塞模式(队列为空时该方法阻塞等待)
     * @param source 源队列
     * @param destination 目标队列
     * @param timeout 阻塞等待超时时间
     * @return
     */
    public byte[] brpoplpush(byte[] source, byte[] destination, int timeout) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        byte[] res = null;
        try{
            if( jedis != null ){
                res = jedis.brpoplpush(source,destination,timeout);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常", e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 修改key对应list指定下标index的元素值
     * @param key
     * @param index
     * @param val
     * @return
     */
    public String lset(String key, int index, String val) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        String res = "";
        try{
            if( jedis != null ){
                res = jedis.lset(key, index, val);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + val, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 获取key对应list的长度
     * @param key
     * @return
     */
    public Long llen(String key) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        Long res = -1L;
        try{
            if( jedis != null ){
                res = jedis.llen(key);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 获取key对应list下标为index的元素
     * @param key
     * @param index
     * @return
     */
    public String lindex(String key, int index) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        String res = "";
        try{
            if( jedis != null ){
                res = jedis.lindex(key, index);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + index, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 将key对应list里面的元素从小到大顺序排列
     * @param key
     * @return
     */
    public List<String> sort(String key) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        List<String> list = null;
        try{
            if( jedis != null ){
                list = jedis.sort(key);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key, e);
        }finally {
            redisPool.close(jedis);
        }
        return list;
    }

    /**
     *
     * 集合（Set）操作
     *
     */

    /**
     * 添加一个Set
     * @param key
     * @param members
     * @return
     */
    public Long sadd(String key, String...members) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        Long res = -1L;
        try{
            if( jedis != null ){
                res = jedis.sadd(key, members);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + Arrays.asList(members), e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 获取key对应set的所有元素
     * @param key
     * @return
     */
    public Set<String> smembers(String key) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        Set<String> set = null;
        try{
            if( jedis != null ){
                set = jedis.smembers(key);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key, e);
        }finally {
            redisPool.close(jedis);
        }
        return set;
    }

    /**
     * 删除一个值为val的元素
     * @param key
     * @param val
     * @return
     */
    public Long srem(String key, String val) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        Long res = -1L;
        try{
            if( jedis != null ){
                res = jedis.srem(key, val);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + val, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 删除值为v1，v2，...的元素
     * @param key
     * @param vals
     * @return
     */
    public Long srem(String key, String...vals) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        Long res = -1L;
        try{
            if( jedis != null ){
                res = jedis.srem(key, vals);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + Arrays.asList(vals), e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 随机出栈set里的一个元素
     * @param key
     * @return
     */
    public String spop(String key) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        String res = "";
        try{
            if( jedis != null ){
                res = jedis.spop(key);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 获取set中的元素个数
     * @param key
     * @return
     */
    public Long scard(String key) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        Long res = -1L;
        try{
            if( jedis != null ){
                res = jedis.scard(key);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 将元素val从集合key1剪切到key2
     * @param key1
     * @param key2
     * @param val
     * @return
     */
    public Long smove(String key1, String key2, String val) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        Long res = -1L;
        try{
            if( jedis != null ){
                res = jedis.smove(key1, key2, val);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key1 + " , " + key2 + " , " + val, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 获取集合key1和集合key2的交集
     * @param key1
     * @param key2
     * @return
     */
    public Set<String> sinter(String key1, String key2) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        Set<String> set = null;
        try{
            if( jedis != null ){
                set = jedis.sinter(key1, key2);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key1 + " , " + key2, e);
        }finally {
            redisPool.close(jedis);
        }
        return set;
    }

    /**
     * 获取集合key1和集合key2的并集
     * @param key1
     * @param key2
     * @return
     */
    public Set<String> sunion(String key1, String key2) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        Set<String> set = null;
        try{
            if( jedis != null ){
                set = jedis.sunion(key1, key2);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key1 + " , " + key2, e);
        }finally {
            redisPool.close(jedis);
        }
        return set;
    }

    /**
     * 获取集合key1和集合key2的差集
     * @param key1
     * @param key2
     * @return
     */
    public Set<String> sdiff(String key1, String key2) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        Set<String> set = null;
        try{
            if( jedis != null ){
                set = jedis.sdiff(key1, key2);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key1 + " , " + key2, e);
        }finally {
            redisPool.close(jedis);
        }
        return set;
    }

    /**
     *
     * 哈希（Hash）操作
     */

    /**
     * 添加一个Hash
     * @param key
     * @param map
     * @return
     */
    public String hmset(String key, Map map) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        String res = "";
        try{
            if( jedis != null ){
                res = jedis.hmset(key, map);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + map, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 往Hash插入一个元素(k, v)
     * @param key
     * @param k
     * @param v
     * @return
     */
    public Long hset(String key, String k, String v) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        Long res = -1L;
        try{
            if( jedis != null ){
                res = jedis.hset(key, k, v);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + k + " , " + v, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 获取Hash的所有(k, v)元素
     * @param key
     * @return
     */
    public Map<String, String> hgetAll(String key) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        Map<String, String> map = null;
        try{
            if( jedis != null ){
                map = jedis.hgetAll(key);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key, e);
        }finally {
            redisPool.close(jedis);
        }
        return map;
    }

    /**
     * 存入 hash类型的一个对象
     * @params  key 键
     * @params  field 字段
     * @params  t 对象
     * @returns
     */
    public <T>Long hset(String key,String field, T t){
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        Long res = -1L;
        try{
            if( jedis != null ){
                byte[] obj = SerializeUtil.protostuffSerialize(t);
                res = jedis.hset(key.getBytes(StandardCharsets.UTF_8),field.getBytes(StandardCharsets.UTF_8),obj);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + field+ " , " + t, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }
    public <T>Long hset(String key,Integer field, T t){
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        Long res = -1L;
        try{
            if( jedis != null ){
                byte[] obj = SerializeUtil.protostuffSerialize(t);
                res = jedis.hset(key.getBytes(StandardCharsets.UTF_8),ByteBuffer.allocate(4).putInt(field).array(),obj);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + field+ " , " + t, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     *  存入 hash 类型数据
     * @param key reids的key
     * @param field hash的field
     * @param t hash的值对象
     * @params  seconds 有效秒数 当seconds>0时才会设置有效秒数,当seconds<=0时永久有效 例如0,-1都表示永久有效
     * @returns long类型 >=0 说明操作成功 插入的 hash个数, 如果是修改替换key值返回0
     */
    public <T>Long hsetIntFieldJsonValue(String key,Integer field, T t,final long seconds) {
        long res = -1L;
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try {
            if( jedis != null ){
                byte[] bk = key.getBytes(StandardCharsets.UTF_8);//redis的key
                byte[] obj = JacksonUtil.serialize(t);
                res = jedis.hset(bk, JacksonUtil.serialize(field), obj);
                if( res >=0 && seconds > 0){
                    jedis.expire(bk, seconds);//有效时间
                }
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + field+ " , " + t, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     *  存入 hash 类型数据
     * @param key reids的key
     * @param field hash的field
     * @param t hash的值对象
     * @params  seconds 有效秒数 当seconds>0时才会设置有效秒数,当seconds<=0时永久有效 例如0,-1都表示永久有效
     * @returns long类型 >=0 说明操作成功 插入的 hash个数, 如果是修改替换key值返回0
     */
    public <T>Long hsetJsonValue(String key,String field, T t,final long seconds) {
        long res = -1L;
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try {
            if( jedis != null ){
                byte[] bk = key.getBytes(StandardCharsets.UTF_8);//redis的key
                byte[] obj = JacksonUtil.serialize(t);
                res = jedis.hset(bk, field.getBytes(StandardCharsets.UTF_8), obj);
                if( res >=0 && seconds > 0){
                    jedis.expire(bk, seconds);//有效时间
                }
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + field+ " , " + t, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;

    }

    /**
     * 取出 hash类型的一个对象
     * @params  key 键
     * @params  field 字段
     * @params  clazz 对象类型
     * @returns 一个对象
     */
    public <T>T hget(String key,String field, Class<T> clazz){
        T object = null;
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            if( jedis != null ){
                byte[] bytes = jedis.hget(key.getBytes(StandardCharsets.UTF_8),field.getBytes(StandardCharsets.UTF_8));
                if (bytes != null && bytes.length > 0) object = SerializeUtil.protostuffDeserialize(bytes, clazz);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + field+ " , " + clazz, e);
        }finally {
            redisPool.close(jedis);
        }
        return object;
    }
    /**
     * 取出 hash类型的一个对象
     * @params  key 键
     * @params  field 字段
     * @params  clazz 对象类型
     * @returns 一个对象
     */
    public <T>T hget(String key,Integer field, Class<T> clazz){
        T object = null;
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            if( jedis != null ){
                byte[] bytes = jedis.hget(key.getBytes(StandardCharsets.UTF_8),ByteBuffer.allocate(4).putInt(field).array());
                if (bytes != null && bytes.length > 0) object = SerializeUtil.protostuffDeserialize(bytes, clazz);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + field+ " , " + clazz, e);
        }finally {
            redisPool.close(jedis);
        }
        return object;
    }
    public <T>T hgetIntFieldJsonValue(String key,Integer field, Class<T> clazz) {
        T object = null;
        if( field == null ){
            log.warn("hgetIntFieldJsonValue 非法参数 key={} field={}", key, field);
            return object;
        }
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try {
            if( jedis != null ){
                byte[] bytes = jedis.hget(key.getBytes(StandardCharsets.UTF_8), JacksonUtil.serialize(field));
                if (bytes != null && bytes.length > 0)  object = JacksonUtil.deserialize(bytes, clazz);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + field+ " , " + clazz, e);
        }finally {
            redisPool.close(jedis);
        }
        return object;
    }
    public <T> T hgetIntFieldListValue(String key,Integer field, TypeReference<T> typeReference) {
        T t = null;
        if( field == null ){
            log.warn("hgetIntFieldListValue 非法参数 key={} field={}", key, field);
            return t;
        }
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try {
            if( jedis != null ){
                byte[] bytes = jedis.hget(key.getBytes(StandardCharsets.UTF_8), JacksonUtil.serialize(field));
                if (bytes != null && bytes.length > 0)  t = JacksonUtil.deserialize(bytes, typeReference);
            }
        } catch (Exception e) {
            log.error("Jedis操作数据 异常 " + key + " , " + field+ " , " + typeReference, e);
        } finally {
            redisPool.close(jedis);
        }
        return t;
    }
    public <T>T hgetJsonValue(String key,String field, Class<T> clazz) {
        T object = null;
        if( field == null ){
            log.warn("hgetJsonValue 非法参数 key={} field={}", key, field);
            return object;
        }
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try {
            if( jedis != null ){
                byte[] bytes = jedis.hget(key.getBytes(StandardCharsets.UTF_8), field.getBytes(StandardCharsets.UTF_8));
                if (bytes != null && bytes.length > 0) object = JacksonUtil.deserialize(bytes, clazz);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + field+ " , " + clazz, e);
        }finally {
            redisPool.close(jedis);
        }
        return object;
    }
    public <T>T hgetListValue(String key,String field, TypeReference<T> typeReference) {
        T t = null;
        if( field == null ){
            log.warn("hgetListValue 非法参数 key={} field={}", key, field);
            return t;
        }
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try {
            if( jedis != null ){
                byte[] bytes = jedis.hget(key.getBytes(StandardCharsets.UTF_8), field.getBytes(StandardCharsets.UTF_8));
                if (bytes != null && bytes.length > 0) t = JacksonUtil.deserialize(bytes,typeReference);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + field+ " , " + typeReference, e);
        }finally {
            redisPool.close(jedis);
        }
        return t;
    }
    /**
     * 存入 hash类型的  Map<String,T>对象集合
     * @params  key 键
     * @params  Map<String,T> 对象集合
     * @params  seconds 有效秒数 大于零才会设置
     * @returns String类型 返回 "OK" 说明操作成功
     */
    public <T>String hmsetStringFieldJsonValue(String key, Map<String,T> map,final long seconds){
        String res = "FAIL";
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            if( jedis != null ){
                byte[] bk = key.getBytes(StandardCharsets.UTF_8);//redis的key
                Map<byte[],byte[]> valueMap = new HashMap<>();
                map.forEach((k,v)-> valueMap.put(k.getBytes(StandardCharsets.UTF_8), JacksonUtil.serialize(v)));
                res = jedis.hmset(bk,valueMap);
                if( "OK".equals(res) && seconds > 0L ){//存入成功
                    jedis.expire(bk, seconds);//设置有效时间
                }
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + map, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }
    /**
     * 存入 hash类型的 Map<Integer,List<T>> 对象集合
     * @params  key 键
     * @params  Map<Integer,List<T>> 对象集合
     * @params  seconds 有效秒数 大于零才会设置
     * @returns long类型 >=0 说明操作成功 插入的 hash个数, 如果是修改替换key值返回0
     * @returns String类型 返回 "OK" 说明操作成功
     */
    public <T>String hmsetIntFieldListValue(String key, Map<Integer,List<T>> map,final long seconds) {
        String res = "FAIL";
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try {
            if( jedis != null ){
                byte[] bk = key.getBytes(StandardCharsets.UTF_8);//redis的key
                Map<byte[], byte[]> valueMap = new HashMap<>();
                map.forEach((k, v) -> {
                    byte[] keyBytes = JacksonUtil.serialize(k);//键字节
                    byte[] valueBytes = JacksonUtil.serialize(v);//值字节
                    if( keyBytes != null && valueBytes != null ){
                        valueMap.put(keyBytes, valueBytes);
                    }
                });
                if( valueMap.size() > 0 ){//有hash值才存入redis
                    res = jedis.hmset(bk, valueMap);
                    if ("OK".equals(res) && seconds > 0L) {//存入成功
                        jedis.expire(bk, seconds);//设置有效时间
                    }
                }
            }
        } catch (Exception e) {
            log.error("Jedis操作数据 异常 " + key + " , " + map, e);
        } finally {
            redisPool.close(jedis);
        }
        return res;
    }
    /**
     * 存入 hash类型的 Map<String,List<T>> 对象集合
     * @params  key 键
     * @params   Map<String,List<T>> 对象集合
     * @params  seconds 有效秒数 大于零才会设置
     * @returns String类型 返回 "OK" 说明操作成功
     */
    public <T>String hmsetStringFieldListValue(String key, Map<String,List<T>> map,final long seconds) {
        String res = "FAIL";
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try {
            if( jedis != null ){
                byte[] bk = key.getBytes(StandardCharsets.UTF_8);//redis的key
                Map<byte[], byte[]> valueMap = new HashMap<>();
                map.forEach((k, v) -> valueMap.put(k.getBytes(StandardCharsets.UTF_8), JacksonUtil.serialize(v)));
                res = jedis.hmset(bk, valueMap);
                if ("OK".equals(res) && seconds > 0L) {//存入成功
                    jedis.expire(bk, seconds);//设置有效时间
                }
            }
        } catch (Exception e) {
            log.error("Jedis操作数据 异常 " + key + " , " + map, e);
        } finally {
            redisPool.close(jedis);
        }
        return res;
    }
    /**
     * 取出 hash类型的 Map<Integer,List<T>> 对象集合
     * @params  key 键
     * @params  field 字段Integer类型
     * @params  clazz 对象类型
     * @returns 对象集合 Map<Integer,List<T>>
     */
    public <T> ConcurrentHashMap<Integer,T> hgetIntFieldListValue(String key, TypeReference<T> typeReference) {
        ConcurrentHashMap<Integer,T> resMap = null;
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try {
            if( jedis != null ){
                Map<byte[], byte[]> map = jedis.hgetAll(key.getBytes(StandardCharsets.UTF_8));
                if (map == null) return null;
                ConcurrentHashMap<Integer,T> resultMap = new ConcurrentHashMap<>();
                T t = null;
                for (Map.Entry<byte[], byte[]> entry : map.entrySet()) {
                    t = JacksonUtil.deserialize(entry.getValue(), typeReference);
                    resultMap.put(JacksonUtil.deserialize(entry.getKey(), Integer.class), t);
                }
                resMap = resultMap;
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + typeReference, e);
        }finally {
            redisPool.close(jedis);
        }
        return resMap;
    }
    /**
     * 取出 hash类型的 Map<String,List<T>> 对象集合
     * @params  key 键
     * @params  field 字段Integer类型
     * @params  clazz 对象类型
     * @returns 对象集合 Map<String,List<T>>
     */
    public <T> ConcurrentHashMap<String,T> hgetStringFieldListValue(String key, TypeReference<T> typeReference) {
        ConcurrentHashMap<String,T> resMap = null;
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try {
            if( jedis != null ){
                Map<byte[], byte[]> map = jedis.hgetAll(key.getBytes(StandardCharsets.UTF_8));
                if (map == null) return null;
                ConcurrentHashMap<String,T> resultMap = new ConcurrentHashMap<>();
                T t = null;
                for (Map.Entry<byte[], byte[]> entry : map.entrySet()) {
                    t = JacksonUtil.deserialize(entry.getValue(), typeReference);
                    resultMap.put(new String(entry.getKey(), StandardCharsets.UTF_8), t);
                }
                resMap = resultMap;
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + typeReference, e);
        }finally {
            redisPool.close(jedis);
        }
        return resMap;
    }

    /**
     * 存入 hash类型的 对象集合
     * @params  key 键
     * @params  map 对象集合
     * @returns
     */
    public <T>Long hsetIntField(String key, Map<Integer,T> map){
        Long res = -1L;
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            if( jedis != null ){
                Map<byte[],byte[]> valueMap = new HashMap<>();
                map.forEach((k,v)-> valueMap.put(ByteBuffer.allocate(4).putInt(k).array(),SerializeUtil.protostuffSerialize(v)));
                res = jedis.hset(key.getBytes(StandardCharsets.UTF_8),valueMap);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + map, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }
    /**
     * 存入 hash类型的 Map<Integer,T> 对象集合
     * @params  key 键
     * @params  Map<Integer,T> 对象集合
     * @params  seconds 有效秒数 大于零才会设置
     * @returns long类型 >=0 说明操作成功 插入的 hash个数, 如果是修改替换key值返回0
     * @returns String类型 返回 "OK" 说明操作成功
     */
    public <T>String hmsetIntFieldJsonValue(String key, Map<Integer,T> map,final long seconds) {
        String res = "FAIL";//默认失败
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try {
            if( jedis != null ){
                byte[] bk = key.getBytes(StandardCharsets.UTF_8);//redis的key
                Map<byte[],byte[]> valueMap = new HashMap<>();
                map.forEach((k,v)-> valueMap.put(JacksonUtil.serialize(k), JacksonUtil.serialize(v)));
                res = jedis.hmset(bk, valueMap);
                if( "OK".equals(res) && seconds > 0L ){
                    jedis.expire(bk,seconds);//设置有效时间
                }
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + map, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 取出 hash类型的 对象集合
     * @params  key 键
     * @params  field 字段
     * @params  clazz 对象类型
     * @returns 一个对象
     */
    public <T> ConcurrentHashMap<String,T> hgetStringFieldJsonValue(String key, Class<T> clazz) {
        ConcurrentHashMap<String,T> resMap = null;
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try {
            if( jedis != null ){
                Map<byte[], byte[]> map = jedis.hgetAll(key.getBytes(StandardCharsets.UTF_8));
                if (map == null) return null;
                ConcurrentHashMap<String, T> resultMap = new ConcurrentHashMap<>();
                map.forEach((k, v) -> resultMap.put(new String(k, StandardCharsets.UTF_8), JacksonUtil.deserialize(v, clazz)));
                resMap = resultMap;
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + clazz, e);
        }finally {
            redisPool.close(jedis);
        }
        return resMap;
    }
    /**
     * 取出 hash类型的 对象集合
     * @params  key 键
     * @params  field 字段
     * @params  clazz 对象类型
     * @returns 一个对象
     */
    public <T> ConcurrentHashMap<Integer,T> hgetIntField(String key,Class<T> clazz){
        ConcurrentHashMap<Integer,T> resMap = null;
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            if( jedis != null ){
                Map<byte[], byte[]> map = jedis.hgetAll(key.getBytes(StandardCharsets.UTF_8));
                if(map == null) return null;
                ConcurrentHashMap<Integer,T> resultMap = new ConcurrentHashMap<>();
                map.forEach((k,v)-> resultMap.put(ByteBuffer.wrap(k).getInt(),SerializeUtil.protostuffDeserialize(v,clazz)));
                resMap = resultMap;
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + clazz, e);
        }finally {
            redisPool.close(jedis);
        }
        return resMap;
    }
    public <T> ConcurrentHashMap<Integer,T> hgetIntFieldJsonValue(String key,Class<T> clazz) {
        ConcurrentHashMap<Integer,T> resMap = null;
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try {
            if( jedis != null ){
                Map<byte[], byte[]> map = jedis.hgetAll(key.getBytes(StandardCharsets.UTF_8));
                if (map == null) return null;
                ConcurrentHashMap<Integer, T> resultMap = new ConcurrentHashMap<>();
                map.forEach((k, v) -> resultMap.put(JacksonUtil.deserialize(k, Integer.class), JacksonUtil.deserialize(v, clazz)));
                resMap = resultMap;
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + clazz, e);
        }finally {
            redisPool.close(jedis);
        }
        return resMap;
    }

    /**
     * 获取Hash所有元素的Key
     * @param key
     * @return
     */
    public Set<String> hkeys(String key) {
        Set<String> set = Collections.emptySet();
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            if( jedis != null ){
                set = jedis.hkeys(key);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key, e);
        }finally {
            redisPool.close(jedis);
        }
        return set;
    }

    /**
     * 获取Hash所有元素的Value
     * @param key
     * @return
     */
    public List<String> hvals(String key) {
        List<String> list = null;
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            list = jedis.hvals(key);
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key, e);
        }finally {
            redisPool.close(jedis);
        }
        return list;
    }

    /**
     * 把Hash中k对应元素的val+=step，step可为负数
     * @param key
     * @param k
     * @param step
     * @return
     */
    public Long hincrBy(String key, String k, int step) {
        Long res = -1L;
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            if( jedis != null ){
                res = jedis.hincrBy(key, k, step);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + k, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 从Hash删除一个或多个字符串类型的field
     * @param key
     * @param field 字段是String 类型
     * @return
     */
    public Long hdel(String key, String... field) {
        Long res = -1L;
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            if( jedis != null ){
                res = jedis.hdel(key,field);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + Arrays.asList(field), e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }
    /**
     * 从Hash删除一个int类型的field
     * @param key
     * @param field 字段是Integer 类型
     * @return
     */
    public Long hdel(String key, Integer field) {
        Long res = -1L;
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            if( jedis != null ){
                res = jedis.hdel(key.getBytes(StandardCharsets.UTF_8),JacksonUtil.serialize(field));
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + Arrays.asList(field), e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }
    /**
     * 从Hash删除多个int类型的field
     * @param key
     * @param field 字段是Integer 类型
     * @return
     */
    public Long hdel(String key, Integer... field) {
        Long res = -1L;
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            if( jedis != null ){
                byte[][] fieldBytes = new byte[field.length][];
                for (int i = 0; i < field.length; i++) {//遍历每一个field
                    fieldBytes[i] = JacksonUtil.serialize(field[i]);
                }
                res = jedis.hdel(key.getBytes(StandardCharsets.UTF_8),fieldBytes);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + Arrays.asList(field), e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }
    public Long hdel(final byte[] key, final byte[]... fields) {
        Long res = -1L;
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            if( jedis != null ){
                res = jedis.hdel(key,fields);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常", e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 获取Hash中元素个数
     * @param key
     * @return
     */
    public Long hlen(String key) {
        Long res = -1L;
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            if( jedis != null ){
                res = jedis.hlen(key);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 判断Hash是否存在k对应元素
     * @param key
     * @param k
     * @return
     */
    public boolean hexists(String key, String k) {
        boolean res = false;
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            if( jedis != null ){
                res = jedis.hexists(key, k);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + k, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 获取Hash中一个或多个元素value
     * @param key
     * @param k
     * @return
     */
    public List<String> hmget(String key, String[] k) {
        List<String> list = null;
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            if( jedis != null ){
                list = jedis.hmget(key, k);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + Arrays.asList(k), e);
        }finally {
            redisPool.close(jedis);
        }
        return list;
    }

    /**
     *
     * 有序集合（Zsort）操作
     */

    /**
     * 添加一个ZSet
     * @param key
     * @param map
     * @return
     */
    public Long zadd(String key, Map<String, Double> map) {
        Long res = -1L;
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            if( jedis != null ){
                res = jedis.zadd(key, map);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + map, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 将数据传入 stream
     * @param key  队列名称
     * @param id  数据ID
     * @param map 数据
     * @return stream 中数据的唯一标志
     */
    public StreamEntryID xadd(String key,final StreamEntryID id, Map map){
        StreamEntryID res = null;
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            if( jedis != null ){
                res = jedis.xadd(key, id, map);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + id + " , " + map, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 阻塞 获取steam
     * @param count 批量取出个数
     * @param block 阻塞超时时间
     * @param streams 订阅的队列名称和消息ID
     * @return 返回的队列名称和消息数据
     */
    public List<Map.Entry<String, List<StreamEntry>>> xread(final int count, final long block, Map<String, StreamEntryID> streams){
        List<Map.Entry<String, List<StreamEntry>>> list = null;
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            if( jedis != null ){
                XReadParams xReadParams = XReadParams.xReadParams();
                xReadParams.count(count);//批量取出个数
                xReadParams.block(0);//阻塞超时时间
                list = jedis.xread(xReadParams,streams);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + count + " , " + block + " , " + streams, e);
        }finally {
            redisPool.close(jedis);
        }
        return list;
    }

    /**
     * 多个订阅者 给队列创建分组
     * @param key 队列名称
     * @param groupname 分组名称
     * @param id 分组唯一ID
     * @param makeStream
     * @return
     */
    public String xgroupCreate(String key, String groupname, StreamEntryID id, boolean makeStream){
        String res = "";
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            if( jedis != null ){
                res = jedis.xgroupCreate(key,groupname, id, makeStream);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + groupname + " , " + id, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }


    /**
     * 多个订阅者  阻塞 获取steam
     * @param groupname
     * @param consumer
     * @param count
     * @param block
     * @param noAck
     * @param streams
     * @return
     */
    public List<Map.Entry<String, List<StreamEntry>>> xreadGroup(String groupname, String consumer, int count, final long block, final boolean noAck,  Map<String, StreamEntryID> streams){
        List<Map.Entry<String, List<StreamEntry>>> list = null;
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            if( jedis != null ){
                XReadGroupParams xReadGroupParams = XReadGroupParams.xReadGroupParams();
                xReadGroupParams.count(count);//批量取出个数
                xReadGroupParams.block(0);//阻塞超时时间
                if( noAck ) xReadGroupParams.noAck();//如果不需要ACK
                list = jedis.xreadGroup(groupname, consumer, xReadGroupParams, streams);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + groupname + " , " + consumer + " , " + count, e);
        }finally {
            redisPool.close(jedis);
        }
        return list;
    }

    /**
     *  ACK 队列steam
     * @param key 队列
     * @param group 消费组
     * @param ids 消息ID
     * @return
     */
    public long xack(String key, String group,  StreamEntryID... ids){
        Long res = -1L;
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            if( jedis != null ){
                res = jedis.xack(key, group, ids);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + group + " , " + Arrays.asList(ids), e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }


    /**
     * 往ZSet插入一个元素(score,member)
     * @param key
     * @param score
     * @param member
     * @return
     */
    public Long zadd(String key, double score, String member) {
        Long res = -1L;
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            if( jedis != null ){
                res = jedis.zadd(key, score, member);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + score + " , " + member, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 获取ZSet里下标[start, end]区间的元素val
     * @param key
     * @param start
     * @param end
     * @return
     */
    public List<String> zrange(String key, int start, int end) {
        List<String> list = null;
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            if( jedis != null ){
                list = jedis.zrange(key, start, end);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + start + " , " + end, e);
        }finally {
            redisPool.close(jedis);
        }
        return list;
    }

    /**
     * 获取ZSet里下标[start, end]区间的元素(score, val)
     * @param key
     * @param start
     * @param end
     * @return
     */
    public List<Tuple> zrangeWithScores(String key, int start, int end) {
        List<Tuple> list = null;
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            if( jedis != null ){
                list = jedis.zrangeWithScores(key, start, end);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + start + " , " + end, e);
        }finally {
            redisPool.close(jedis);
        }
        return list;
    }

    /**
     * 获取ZSet里score[min, max]区间元素的val
     * @param key
     * @param min
     * @param max
     * @return
     */
    public List<String> zrangeByScore(String key, double min, double max) {
        List<String> list = null;
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            if( jedis != null ){
                list = jedis.zrangeByScore(key, min, max);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + min + " , " + max, e);
        }finally {
            redisPool.close(jedis);
        }
        return list;
    }

    /**
     * 获取ZSet里score[start, end]区间的元素(score, val)
     * @param key
     * @param min
     * @param max
     * @return
     */
    public List<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
        List<Tuple> list = null;
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            if( jedis != null ){
                list = jedis.zrangeByScoreWithScores(key, min, max);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + min + " , " + max, e);
        }finally {
            redisPool.close(jedis);
        }
        return list;
    }

    /**
     * 获取ZSet里val为value的元素的score
     * @param key
     * @param value
     * @return
     */
    public Double zscore(String key, String value) {
        Double res = 0D;
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            if( jedis != null ){
                res = jedis.zscore(key, value);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + value, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 获取ZSet里val为value的元素的score排名
     * @param key
     * @param value
     * @return
     */
    public Long zrank(String key, String value) {
        Long res = -1L;
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            if( jedis != null ){
                res = jedis.zrank(key, value);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + value, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 删除ZSet里val为value的元素
     * @param key
     * @param value
     * @return
     */
    public Long zrem(String key, String value) {
        Long res = -1L;
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            if( jedis != null ){
                res = jedis.zrem(key, value);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + value, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    public Long zremRangeByScore(String key, double min,double max) {
        Long res = -1L;
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            if( jedis != null ){
                res = jedis.zremrangeByScore(key, min,max);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + min + " , " + max, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 获取ZSet的元素个数
     * @param key
     * @return
     */
    public Long zcard(String key) {
        Long res = -1L;
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            if( jedis != null ){
                res = jedis.zcard(key);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 获取ZSet中score在[min, max]区间的元素个数
     * @param key
     * @param min
     * @param max
     * @return
     */
    public Long zcount(String key, double min, double max) {
        Long res = -1L;
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            if( jedis != null ){
                res = jedis.zcount(key, min, max);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + min + " , " + max, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 把ZSet中val为value的元素score+=increment
     * @param key
     * @param increment
     * @param value
     * @return
     */
    public Double zincrby(String key, double increment, String value) {
        Double res = 0D;
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            if( jedis != null ){
                res = jedis.zincrby(key, increment, value);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + increment + " , " + value, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * 排序操作
     */

    /**
     * 队列按首字母a-z排列
     * @param key
     * @return
     */
    public List<String> sortByAlpha(String key) {
        List<String> list = null;
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            if( jedis != null ){
                list = jedis.sort(key, new SortingParams().alpha());
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key, e);
        }finally {
            redisPool.close(jedis);
        }
        return list;
    }

    /**
     * 队列按数字升序排列
     * @param key
     * @return
     */
    public List<String> sortByAsc(String key) {
        List<String> list = null;
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            if( jedis != null ){
                list = jedis.sort(key, new SortingParams().asc());
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key, e);
        }finally {
            redisPool.close(jedis);
        }
        return list;
    }

    /**
     * 队列按数字降序排列
     * @param key
     * @return
     */
    public List<String> sortByDesc(String key) {
        List<String> list = null;
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            if( jedis != null ){
                list = jedis.sort(key, new SortingParams().desc());
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key, e);
        }finally {
            redisPool.close(jedis);
        }
        return list;
    }

    /**
     * java对象序列化存储到Key
     * @param key
     * @param t
     * @param <T>
     * @return
     */
    public <T> String setObject(String key, T t) {
        String res = "";
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            if( jedis != null ){
                byte[] bytes = SerializeUtil.protostuffSerialize(t);
                res = jedis.set(key.getBytes(StandardCharsets.UTF_8), bytes);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + t, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * java对象反序列化
     * @param key
     * @param <T>
     * @return
     */
    public <T> T getObject(String key, Class<T> clazz) {
        T res = null;
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            if( jedis != null ){
                byte[] bytes = jedis.get(key.getBytes(StandardCharsets.UTF_8));
                res = SerializeUtil.protostuffDeserialize(bytes, clazz);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + clazz, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }


    /**
     * java对象反序列化
     * @param key
     * @param clazz
     * 对应 {@link JedisUtil#setObj(String, Object)}
     * @return
     */
    public <T> T getObj(String key, Class<T> clazz) {
        T res = null;
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            if( jedis != null ){
                byte[] bytes = jedis.get(key.getBytes(StandardCharsets.UTF_8));
                if( bytes != null && bytes.length > 0 ) res = JacksonUtil.deserialize(bytes, clazz);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + clazz, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

    /**
     * java对象序列化存储到Key
     * @param key
     * @param t
     * 对应 {@link JedisUtil#getObj(String, Class)}
     * @return
     */
    public <T> String setObj(String key, T t) {
        String res = "";
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            if( jedis != null ){
                byte[] bytes = JacksonUtil.serialize(t);
                res = jedis.set(key.getBytes(StandardCharsets.UTF_8), bytes);
            }
        }catch (Exception e){
            log.error("Jedis操作数据 异常 " + key + " , " + t, e);
        }finally {
            redisPool.close(jedis);
        }
        return res;
    }

}

