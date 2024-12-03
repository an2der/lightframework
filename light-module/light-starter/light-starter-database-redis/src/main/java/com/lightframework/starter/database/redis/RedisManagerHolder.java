package com.lightframework.starter.database.redis;

import com.lightframework.database.redis.RedisManager;
import com.lightframework.util.spring.SpringContextUtil;
import redis.clients.jedis.Jedis;

public class RedisManagerHolder {

    private RedisManagerHolder(){}
    static final String REDIS_MANAGER_NAME = "redisManager";
    private static RedisManager redisManager = null;

    public static RedisManager getRedisManager(){
        if(redisManager == null){
            redisManager = SpringContextUtil.getBean(REDIS_MANAGER_NAME);
        }
        return redisManager;
    }

    public static Jedis autoCloseClient(){
        return getRedisManager().autoCloseClient();
    }

    public static Jedis autoCloseNoThrowExClient(){
        return getRedisManager().autoCloseNoThrowExClient();
    }

    public static RedisManager.RedisUtil util(){
        return getRedisManager().util();
    }
}
