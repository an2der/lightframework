package com.lightframework.starter.database.redis;

import com.lightframework.database.redis.RedisConfig;
import com.lightframework.database.redis.RedisManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "redis",name = "enabled",havingValue = "true",matchIfMissing = true)
@Slf4j
public class RedisStarter {

    @Autowired
    private RedisProperties redisProperties;

    @Bean(RedisManagerHolder.REDIS_MANAGER_NAME)
    public RedisManager buildRedisManager(){
        RedisConfig redisConfig = new RedisConfig();
        BeanUtils.copyProperties(redisProperties,redisConfig);
        BeanUtils.copyProperties(redisProperties.getPoolConfig(),redisConfig.getPoolConfig());
        RedisManager redisManager = new RedisManager(redisConfig);
        return redisManager;
    }

}
