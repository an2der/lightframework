package com.lightframework.database.redis.queue;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lightframework.database.redis.config.RedisPoolManager;
import com.lightframework.database.redis.jedis.JedisUtil;
import com.lightframework.database.redis.jedis.RedisPool;
import com.lightframework.util.serialize.SerializeUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.resps.StreamEntry;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Redis消息队列工具
 * 支持五种工作模式
 * 1. redis的 发布订阅模式
 * 2. redis的 list队列简单模式
 * 3. redis的 list队列ACK模式
 * 4. redis的 steam队列简单模式
 * 5. redis的 stream队列分组ACK模式
 *
 * @author ：mashuai
 * @date ：2022/05/19 9:24
 */
public class QueueUtil {

    private String redisName;//redis连接名称
    private RedisPool redisPool;//Jedis客户端工具

    private QueueUtil() {}//私有化构造,不允许new对象

    public QueueUtil(String redisName, RedisPool redisPool) {
        this.redisName = redisName;
        this.redisPool = redisPool;
    }

    /**
     * 默认使用 首选数据源
     */
    public static QueueUtil use(){
        return RedisPoolManager.getQueueUtil();
    }
    /**
     * 根据数据源 名称
     */
    public static QueueUtil use(String redisName){
        return RedisPoolManager.getQueueUtil(redisName);
    }


    //--------------------------- 1. redis的 发布订阅模式  开始 --------------------------------

    /**
     * 发布消息
     * redis的 发布订阅模式
     * @param channel 频道/主题
     * @param message 字符串消息
     * @return
     */
    public Long publish(String channel, String message) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            return jedis.publish(channel,message);
        }finally {
            redisPool.close(jedis);
        }
    }
    /**
     * 发布消息
     * redis的 发布订阅模式
     * @param channel 频道/主题
     * @param object 对象消息
     * @return
     */
    public <T>Long publish(String channel, T object) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            byte[] message = SerializeUtil.protostuffSerialize(object);
            return jedis.publish(channel.getBytes(),message);
        }finally {
            redisPool.close(jedis);
        }
    }

    /**
     * 订阅一个或者多个频道 (字符串消息) 阻塞
     * redis的 发布订阅模式
     * @param stringJedisPubSub 订阅回调
     * @param channels 频道/主题
     * @return
     */
    public void subscribe(StringJedisPubSub stringJedisPubSub, String... channels) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            jedis.subscribe(stringJedisPubSub,channels);
        }finally {
            redisPool.close(jedis);
        }
    }
    /**
     * 订阅一个或者多个频道 (对象消息) 阻塞
     * redis的 发布订阅模式
     * @param objectJedisPubSub 订阅回调
     * @param channels 频道/主题
     */
    public void subscribe(ObjectJedisPubSub objectJedisPubSub, String... channels) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            List<byte[]> list = Arrays.stream(channels).map(s -> s.getBytes()).collect(Collectors.toList());
            jedis.subscribe(objectJedisPubSub, list.toArray(new byte[list.size()][]));
        }finally {
            redisPool.close(jedis);
        }
    }
    /**
     * 订阅一个或者多个频道 (字符串消息) 阻塞
     * redis的 发布订阅模式
     * @param stringJedisPubSub 订阅回调
     * @param channels 频道/主题 正则表达式
     * @return
     */
    public void psubscribe(StringJedisPubSub stringJedisPubSub, String... channels) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            jedis.psubscribe(stringJedisPubSub,channels);
        }finally {
            redisPool.close(jedis);
        }
    }
    /**
     * 订阅一个或者多个频道 (对象消息) 阻塞
     * redis的 发布订阅模式
     * @param objectJedisPubSub 订阅回调
     * @param channels 频道/主题 正则表达式
     * @return
     */
    public void psubscribe(ObjectJedisPubSub objectJedisPubSub, String... channels) {
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        try{
            List<byte[]> list = Arrays.stream(channels).map(s -> s.getBytes()).collect(Collectors.toList());
            jedis.psubscribe(objectJedisPubSub, list.toArray(new byte[list.size()][]));
        }finally {
            redisPool.close(jedis);
        }
    }

    //--------------------------- 1. redis的 发布订阅模式  结束 --------------------------------





















    //--------------------------- 2. redis的 list队列简单模式  开始 --------------------------------



    /**
     * 发布消息
     * redis的 list队列简单模式
     * @param key 频道/主题
     * @param value 字符串消息
     * @return
     */
    public Long lpush(String key, String value){
        return JedisUtil.use(redisName).lpush(key, value);
    }
    public Long rpush(String key, String value){
        return JedisUtil.use(redisName).rpush(key, value);
    }

    /**
     * 发布消息
     * redis的 list队列简单模式
     * @param key 频道/主题
     * @param object 对象消息
     * @return
     */
    public Long lpush(String key, Object object){
        return JedisUtil.use(redisName).lpush(key, object);
    }
    public Long rpush(String key, Object object){
        return JedisUtil.use(redisName).rpush(key, object);
    }
    /**
     * 将值插入队列头部,并校验队列最大大小,超过最大长度丢弃掉最老的消息
     * @param key 键
     * @param value 最新值
     * @param maxsize 允许存入的最大大小
     */
    public void lpushCheckLlen(String key, Object value, Long maxsize) {
        JedisUtil.use(redisName).lpushCheckLlen(key, value, maxsize);
    }
    public void replace(String key,Object object){
        //超过最大限制,移除尾部最老的消息,再存入最新的
        JedisUtil.use(redisName).replace(key, object);//插入头部一条消息
    }

    /**
     * 订阅消息 (阻塞模式)
     * redis的 list队列简单模式
     * 阻塞模式(队列为空时该方法阻塞等待)
     * @param key 频道/主题
     * @return 字符串消息
     */
    public String brpop(String key){
        List<String> list = JedisUtil.use(redisName).brpop(key);
        if( list != null && list.size() == 2 ){
            //list.get(0) 就是 key
            return list.get(1);
        }
        return null;
    }
    public String brpop(String key, int timeout){
        List<String> list = JedisUtil.use(redisName).brpop(key, timeout);
        if( list != null && list.size() == 2 ){
            //list.get(0) 就是 key
            return list.get(1);
        }
        return null;
    }
    /**
     * 订阅消息 (阻塞模式)
     * redis的 list队列简单模式
     * 阻塞模式(队列为空时该方法阻塞等待)
     * @param key 频道/主题
     * @return 对象消息
     */
    public <T>T brpop(String key,Class<T> clazz) {
        return JedisUtil.use(redisName).brpop(key, clazz);
    }
    public <T>T brpop(String key,Class<T> clazz, int timeout) {
        return JedisUtil.use(redisName).brpop(key, clazz, timeout);
    }

    /**
     *订阅消息 (阻塞模式)
     *  redis的 list队列简单模式
     *  阻塞模式(队列为空时该方法阻塞等待)
     * @param key  频道/主题
     * @param typeReference 消息是集合类型 泛型类型,例如 new TypeReference<List<RAlarm>>(){};     new TypeReference<Map<String, Object>>(){}
     * @param <T>
     * @return
     */
    public <T>List<T> brpop(String key,TypeReference<List<T>> typeReference) {
        return JedisUtil.use(redisName).brpop(key, typeReference);
    }
    public <T>List<T> brpop(String key,TypeReference<List<T>> typeReference, int timeout) {
        return JedisUtil.use(redisName).brpop(key, typeReference, timeout);
    }

    //--------------------------- 2. redis的 list队列简单模式  结束 --------------------------------











    //--------------------------- 3. redis的 list队列ACK模式  开始 --------------------------------

    // redis的 list队列简单模式和AcK模式的发布消息方法一样

    /**
     * 订阅消息 (阻塞模式) 从 source队列 移动到 destination 队列
     * redis的 list队列ACK模式
     * 阻塞模式(队列为空时该方法阻塞等待)
     * @param source 源队列 频道/主题
     * @param destination ack队列  频道/主题
     * @return 字符串消息
     */
    public String brpoplpush(String source, String destination){
        int timeout = 0;//阻塞等待超时时间毫秒数,0无限等待
        return JedisUtil.use(redisName).brpoplpush(source,destination,timeout);
    }
    /**
     * 订阅消息 (阻塞模式) 从 source队列 移动到 destination 队列
     * redis的 list队列ACK模式
     * 阻塞模式(队列为空时该方法阻塞等待)
     * @param source 源队列 频道/主题
     * @param destination ack队列  频道/主题
     * @return 对象消息
     */
    public <T>T brpoplpush(String source, String destination,Class<T> clazz){
        byte[] sourceKey = source.getBytes(StandardCharsets.UTF_8);
        byte[] destinationKey = destination.getBytes(StandardCharsets.UTF_8);
        int timeout = 0;//阻塞等待超时时间毫秒数,0无限等待
        byte[] bytes = JedisUtil.use(redisName).brpoplpush(sourceKey,destinationKey,timeout);
        return SerializeUtil.protostuffDeserialize(bytes);
    }

    /**
     * ACK 回执成功
     * redis的 list队列ACK模式
     * @param ackKey ack队列  频道/主题
     * @return
     */
    public Long ackList(String ackKey){
        return JedisUtil.use(redisName).del(ackKey);
    }



    //--------------------------- 3. redis的 list队列ACK模式  结束 --------------------------------













    //--------------------------- 4. redis的 steam队列简单模式  开始 --------------------------------

    /**
     * 将数据传入 stream
     * @param key  队列名称
     * @param map 数据
     * @return stream 中数据的唯一标志
     */
    public StreamEntryID xadd(String key, Map<String,String> map){
        return JedisUtil.use(redisName).xadd(key,StreamEntryID.NEW_ENTRY, map);
    }

    /**
     *  阻塞 获取steam
     * @param key 队列名称
     * @param count 批量取出个数
     * @return
     */
    public List<StreamEntry> xread(String key, int count){
        //阻塞等待10分钟
        long block = 600_000;//阻塞等待超时时间毫秒数,值大于零是阻塞毫秒数，值不大于0就是非阻塞模式
        Map<String,StreamEntryID> streams = new HashMap<>();
        streams.put(key, StreamEntryID.LAST_ENTRY);//队列key中最新的消息
        List<Map.Entry<String, List<StreamEntry>>> list= JedisUtil.use(redisName).xread(count, block,streams);//list=[redis_queue_stream_ack=[1655370044773-0 {name=田老板, age=18}]]
        List<StreamEntry> streamEntryList = list.stream().findFirst().get().getValue();
        return streamEntryList;
    }


    //--------------------------- 4. redis的 steam队列简单模式  结束 --------------------------------
















    //--------------------------- 5. redis的 stream队列分组ACK模式  开始 --------------------------------


    /**
     * 多个订阅者 给队列创建分组
     * @param key 队列名称
     * @param groupname 分组名称
     * @param id 分组唯一ID
     * @param makeStream
     * @return
     */
    public String xgroupCreate(String key, String groupname,final StreamEntryID id, boolean makeStream){
        return JedisUtil.use(redisName).xgroupCreate(key,groupname, StreamEntryID.NEW_ENTRY, makeStream);

    }
    public String xgroupCreate(String key, String groupname){
        return xgroupCreate(key,groupname,  new StreamEntryID(), true);
    }


    /**
     * 多个订阅者  阻塞 获取steam
     * @param key 队列名称
     * @param groupname 分组名称
     * @param consumer 消费者名称
     * @param count 批量取出个数
     * @param noAck 自动ACP,不需要手动ACK回执
     * @return
     */
    public List<StreamEntry> xreadGroup(String key,String groupname, String consumer, int count, final boolean noAck){
        //阻塞等待10分钟
        final long block = 60_000;//阻塞等待超时时间毫秒数,值大于零是阻塞毫秒数，值不大于0就是非阻塞模式
        Map<String,StreamEntryID> streams = new HashMap<>();
        streams.put(key, StreamEntryID.UNRECEIVED_ENTRY);//分组的队列key中未标记的消息
        List<Map.Entry<String, List<StreamEntry>>> list=  JedisUtil.use(redisName).xreadGroup(groupname, consumer, count, block, noAck, streams);
        System.out.println("list = " + list);
        List<StreamEntry> streamEntryList = list.stream().findFirst().get().getValue();
        return streamEntryList;

    }

    /**
     *  ACK 队列steam
     * @param key 队列
     * @param group 消费组
     * @param ids 消息ID
     * @return
     */
    public long xack(String key, String group,  StreamEntryID... ids){
        Jedis jedis = redisPool.getConnection();//获取连接,自动重连
        return JedisUtil.use(redisName).xack(key, group, ids);
    }
    public long xack(String key, String group,  String... ids){
        StreamEntryID[] entryIDS = new StreamEntryID[ids.length];
        for (int i = 0; i < ids.length; i++) {
            entryIDS[i] = new StreamEntryID(ids[i]);
        }
        return xack(key, group, entryIDS);
    }
    public long xack(String key, String group,  String id){
        StreamEntryID entryID = new StreamEntryID(id);
        return xack(key, group, entryID);
    }

    //--------------------------- 5. redis的 stream队列分组ACK模式  结束 --------------------------------









}
