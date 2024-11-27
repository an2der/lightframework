package com.lightframework.database.redis.queue;

import com.lightframework.util.serialize.SerializeUtil;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.BinaryJedisPubSub;

import java.nio.charset.Charset;

/** 对象消息的订阅回调类
 * 订阅者根据需要重写该类中的方法 onMessage(String channel, T object) 子类构造必须调用 super(JavaBean.class)
 * @author ：mashuai
 * @date ：2022/05/19 11:03
 */
@Slf4j
public abstract class ObjectJedisPubSub<T> extends BinaryJedisPubSub {

    protected  Class<T> clazz = null;

    private ObjectJedisPubSub() {}
    public ObjectJedisPubSub(Class<T> clazz) { //子类无参构造必须要调用 super(JavaBean.class),用于设置对象的类型
        this.clazz = clazz;
    }

    /** 子类需要实现的方法
     * 订阅消息到达 接收 频道/主题,对象消息
     * @param channel 频道/主题
     * @param object 对象消息
     */
    public abstract void onMessage(String channel, T object);

    /**
     * 订阅消息到达 subscribe()
     * @param channel 频道/主题
     * @param message 对象消息
     */
    @Override
    public void onMessage(byte[] channel, byte[] message) {
        log.debug("ObjectJedisPubSub 订阅消息到达 channel={} message={}",new String(channel),message);
        String topic = new String(channel, Charset.forName("UTF-8"));
        T object = SerializeUtil.protostuffDeserialize(message);
        this.onMessage(topic, object);
    }

    /**
     * 正则表达式订阅消息到达 psubscribe()
     * @param pattern 频道/主题 正则表达式
     * @param channel 发布者发布的 频道/主题
     * @param message
     */
    @Override
    public void onPMessage(byte[] pattern, byte[] channel, byte[] message) {
        log.debug("StringJedisPubSub 正则表达式订阅消息到达 pattern={} channel={} message={}",new String(pattern),new String(channel),message);
    }
    /**
     * 订阅成功
     * @param channel 频道/主题
     * @param subscribedChannels 订阅的 频道/主题 标识
     */
    @Override
    public void onSubscribe(byte[] channel, int subscribedChannels) {
        log.debug("StringJedisPubSub 订阅频道成功 channel={} subscribedChannels={}",new String(channel),subscribedChannels);
    }
    /**
     * 正则表达式频道订阅成功
     * @param pattern 频道/主题 正则表达式
     * @param subscribedChannels 订阅的 频道/主题 标识
     */
    @Override
    public void onPUnsubscribe(byte[] pattern, int subscribedChannels) {
        log.debug("StringJedisPubSub 正则表达式频道订阅成功 pattern={} subscribedChannels={}",new String(pattern),subscribedChannels);
    }
    /**
     * 取消订阅
     * @param channel  频道/主题
     * @param subscribedChannels 取消订阅的 频道/主题 标识
     */
    @Override
    public void onUnsubscribe(byte[] channel, int subscribedChannels) {
        log.debug("StringJedisPubSub 取消订阅成功 channel={} subscribedChannels={}",new String(channel),subscribedChannels);
        //this.unsubscribe(channel);//取消订阅
    }
    /**
     * 正则表达式取消订阅成功
     * @param pattern 频道/主题 正则表达式
     * @param subscribedChannels 订阅的 频道/主题 标识
     */
    @Override
    public void onPSubscribe(byte[] pattern, int subscribedChannels) {
        log.debug("StringJedisPubSub 正则表达式取消订阅成功 pattern={} subscribedChannels={}",new String(pattern),subscribedChannels);
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public void setClazz(Class<T> clazz) {
        this.clazz = clazz;
    }
}
