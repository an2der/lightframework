package com.lightframework.database.redis.queue;

import com.lightframework.common.LightException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisPubSub;

/**
 * @Description Redis Key监听操作
 * @Author hua
 * @Date 2022/6/14
 **/
@Slf4j
@Data
public class RedisKeyListener extends JedisPubSub {

    private Handler handler;

    @Override
    public void onPMessage(String pattern, String channel, String message) {
        if(handler == null){
            throw new LightException("请实例化Handler接口");
        }
        //格式如下：pattern{__keyspace@*__:hello}channel{__keyspace@15__:hello}message{set}
        handler.doHandle(pattern,channel,message);
    }

}
