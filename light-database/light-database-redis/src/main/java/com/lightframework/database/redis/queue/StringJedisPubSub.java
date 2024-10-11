package com.lightframework.database.redis.queue;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisPubSub;

/** 字符串消息的订阅回调类
 *  订阅者根据需要重写该类中的方法
 * @author ：mashuai
 * @date ：2022/05/19 10:23
 */
@Slf4j
public class StringJedisPubSub extends JedisPubSub {

    /**
     * 订阅消息到达 subscribe()
     * @param channel 频道/主题
     * @param message 字符串消息
     */
    public void onMessage(String channel, String message) {
        log.debug("StringJedisPubSub 订阅消息到达 channel={} message={}",channel,message);
    }

    /**
     * 正则表达式订阅消息到达 psubscribe()
     * @param pattern 频道/主题 正则表达式
     * @param channel 发布者发布的 频道/主题
     * @param message
     */
    public void onPMessage(String pattern, String channel, String message) {
        log.debug("StringJedisPubSub 正则表达式订阅消息到达 pattern={} channel={} message={}",pattern,channel,message);
    }

    /**
     * 订阅成功
     * @param channel 频道/主题
     * @param subscribedChannels 订阅的 频道/主题 标识
     */
    public void onSubscribe(String channel, int subscribedChannels) {
        log.debug("StringJedisPubSub 订阅频道成功 channel={} subscribedChannels={}",channel,subscribedChannels);
    }

    /**
     * 正则表达式频道订阅成功
     * @param pattern 频道/主题 正则表达式
     * @param subscribedChannels 订阅的 频道/主题 标识
     */
    public void onPSubscribe(String pattern, int subscribedChannels) {
        log.debug("StringJedisPubSub 正则表达式频道订阅成功 pattern={} subscribedChannels={}",pattern,subscribedChannels);
    }

    /**
     * 取消订阅
     * @param channel  频道/主题
     * @param subscribedChannels 取消订阅的 频道/主题 标识
     */
    public void onUnsubscribe(String channel, int subscribedChannels) {
        log.debug("StringJedisPubSub 取消订阅成功 channel={} subscribedChannels={}",channel,subscribedChannels);
        //this.unsubscribe(channel);//取消订阅
    }

    /**
     * 正则表达式取消订阅成功
     * @param pattern 频道/主题 正则表达式
     * @param subscribedChannels 订阅的 频道/主题 标识
     */
    public void onPUnsubscribe(String pattern, int subscribedChannels) {
        log.debug("StringJedisPubSub 正则表达式取消订阅成功 pattern={} subscribedChannels={}",pattern,subscribedChannels);
    }

    /**
     *  JedisPubSub.ping() 的回调
     * @param pattern
     */
    public void onPong(String pattern) {
        log.debug("StringJedisPubSub this.ping()的回调 pattern={}", pattern);

    }


}
