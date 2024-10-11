package com.lightframework.database.redis.config;

import com.lightframework.database.redis.util.JacksonUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/** redis 多数据源配置
 * @author ：mashuai
 * @date ：2022/08/14 15:52
 */
@Data
@Slf4j
public class JedisConfig {

    private String primary;//默认优先使用的redis连接
    private Map<String,RedisConfig> redisPool;//redis多数据源

    @Data
    public static class RedisConfig{
        private String host;// 182.00.120.165 # 主机地址
        private Integer port;// 6379 # 端口号
        private String password;// # 连接密码
        private Integer database;// # 库索引0-15
        private Integer maxIdle;// 20 # 最大空闲数
        private Integer maxTotal;// 1000 # 最大连接数 0表示无限制
        private Integer connectionTimeout;// 20_000 # 客户端连接超时毫秒数
        private Integer soTimeout;// 10_000 # 读取数据超时毫秒数
        private Integer maxWaitMillis;// 10_000 # 。如果超过此时间将接到异常。设为-1表示无限制。
        private Integer minEvictableIdleTimeMillis;// 300_000 #连接的最小空闲时间 默认1800000毫秒(30分钟)
        private Integer numTestsPerEvictionRun;// 1024 # 每次释放连接的最大数目,默认3
        private Boolean testOnCreate;// true #是否在创建连接池时进行检验,默认为false时无法连接也可以成功创建连接池
        private Boolean testOnBorrow;// true #是否在从池中取出连接前进行检验,如果检验失败,则从池中去除连接并尝试取出另一个
        private Boolean testWhileIdle;// true #在空闲时检查有效性, 默认false
    }

    private static JedisConfig instance = null;//redis配置对象

    /**
     * 获取配置
     * @return 配置对象
     */
    public synchronized static JedisConfig getInstance(){
        if ( instance == null){
            instance = JedisConfigLoader.load();//初始化配置
            log.info("JedisConfig 初始化配置 instance={}", JacksonUtil.toJSON(instance));
        }
        return instance;
    }

}
