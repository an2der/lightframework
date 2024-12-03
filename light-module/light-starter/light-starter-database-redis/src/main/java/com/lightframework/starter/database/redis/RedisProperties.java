package com.lightframework.starter.database.redis;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author yg
 * @Date 2024/12/2 10:01
 */
@Component
@ConfigurationProperties(prefix = "redis")
@Getter
@Setter
public class RedisProperties {

    /**
     * 是否开启redis
     */
    private boolean enabled = true;

    /**
     * 客户端名称
     */
    private String name = "RedisClient";
    /**
     * 主机地址
     */
    private String host = "127.0.0.1";
    /**
     * 端口
     */
    private int port = 6379;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 数据库索引
     */
    private int database = 0;
    /**
     * 连接超时时间 单位：毫秒
     */
    private int connectionTimeout = 2000;
    /**
     * 读取数据超时时间 单位：毫秒
     */
    private int soTimeout = 2000;

    private RedisPoolConfig poolConfig = new RedisPoolConfig();


    @Getter
    @Setter
    public static class RedisPoolConfig{
        /**
         * 连接池中最大连接数
         */
        private int maxTotal = 8;
        /**
         * 连接池中允许的最大空闲连接数
         */
        private int maxIdle = 8;
        /**
         * 当无连接时池中最小的连接数
         */
        private int minIdle = 0;
        /**
         * 连接对象是否后进先出
         */
        private boolean lifo = true;
        /**
         * 当从池中获取资源或者将资源还回池中时 是否使用java.util.concurrent.locks.ReentrantLock.ReentrantLock 的公平锁机制
         */
        private boolean fairness = false;

        /**
         * 对象空闲多久后逐出, 当(空闲时间 > 该值 && 空闲连接 > 最大空闲数)时直接逐出,不再根据MinEvictableIdleTimeMillis判断
         */
        private long softMinEvictableIdleTimeMillis = -1;

        /**
         * 逐出策略
         */
        private String evictionPolicyClassName = "org.apache.commons.pool2.impl.DefaultEvictionPolicy";
        /**
         * 是否开启创建连接时对连接有效性检查
         */
        private boolean testOnCreate = false;
        /**
         * 是否开启向连接池获取连接时对连接有效性检查，检测连接无效时直接移除
         */
        private boolean testOnBorrow = true;
        /**
         * 是否开启向连接池归还连接时对连接有效性检查，检测连接无效时直接移除，不建议开启，减少资源消耗
         */
        private boolean testOnReturn = false;
        /**
         * 是否开启空闲资源检查
         */
        private boolean testWhileIdle = true;
        /**
         * 空闲资源检查周期，单位：毫秒，-1表示不检查
         */
        private long timeBetweenEvictionRunsMillis = 30000L;
        /**
         * 每次逐出检查时,逐出连接的个数,-1表示对所有连接检查
         */
        private int numTestsPerEvictionRun = -1;
        /**
         * 设置连接最小的逐出间隔时间，单位：毫秒
         */
        private long minEvictableIdleTimeMillis = 60000L;
        /**
         * 当资源池连接用尽后，调用者是否等待
         */
        private boolean blockWhenExhausted = true;
        /**
         * 当资源池连接用尽后，调用者最大等待时间(blockWhenExhausted=true时生效)，单位：毫秒，-1表示永不超时，不建议使用默认值
         */
        private long maxWaitMillis = 10000L;
        /**
         * 设置是否启用JMX
         */
        private boolean jmxEnabled = true;
        /**
         * JMX前缀名
         */
        private String jmxNamePrefix = "pool";
        /**
         * JMX基础名
         */
        private String jmxNameBase;
    }

}
