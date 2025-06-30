package com.lightframework.database.redis;

import com.lightframework.util.serialize.SerializeUtil;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author yg
 * @Date 2024/12/2 9:51
 */
@Slf4j
public class RedisManager {

    private final JedisPool jedisPool;

    private RedisConfig redisConfig;

    private RedisUtil redisUtil = new RedisUtil();

    public RedisManager(RedisConfig redisConfig){
        this.redisConfig = redisConfig;
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(redisConfig.getPoolConfig().getMaxTotal());
        jedisPoolConfig.setMaxIdle(redisConfig.getPoolConfig().getMaxIdle());
        jedisPoolConfig.setMinIdle(redisConfig.getPoolConfig().getMinIdle());
        jedisPoolConfig.setLifo(redisConfig.getPoolConfig().isLifo());
        jedisPoolConfig.setFairness(redisConfig.getPoolConfig().isFairness());
        jedisPoolConfig.setSoftMinEvictableIdleTime(Duration.ofMillis(redisConfig.getPoolConfig().getSoftMinEvictableIdleTimeMillis()));
        jedisPoolConfig.setEvictionPolicyClassName(redisConfig.getPoolConfig().getEvictionPolicyClassName());
        jedisPoolConfig.setTestOnCreate(redisConfig.getPoolConfig().isTestOnCreate());
        jedisPoolConfig.setTestOnBorrow(redisConfig.getPoolConfig().isTestOnBorrow());
        jedisPoolConfig.setTestOnReturn(redisConfig.getPoolConfig().isTestOnReturn());
        jedisPoolConfig.setTestWhileIdle(redisConfig.getPoolConfig().isTestWhileIdle());
        jedisPoolConfig.setTimeBetweenEvictionRuns(Duration.ofMillis(redisConfig.getPoolConfig().getTimeBetweenEvictionRunsMillis()));
        jedisPoolConfig.setNumTestsPerEvictionRun(redisConfig.getPoolConfig().getNumTestsPerEvictionRun());
        jedisPoolConfig.setMinEvictableIdleTime(Duration.ofMillis(redisConfig.getPoolConfig().getMinEvictableIdleTimeMillis()));
        jedisPoolConfig.setBlockWhenExhausted(redisConfig.getPoolConfig().isBlockWhenExhausted());
        jedisPoolConfig.setMaxWait(Duration.ofMillis(redisConfig.getPoolConfig().getMaxWaitMillis()));
        jedisPoolConfig.setJmxEnabled(redisConfig.getPoolConfig().isJmxEnabled());
        jedisPoolConfig.setJmxNamePrefix(redisConfig.getPoolConfig().getJmxNamePrefix());
        jedisPoolConfig.setJmxNameBase(redisConfig.getPoolConfig().getJmxNameBase());

        this.jedisPool = new JedisPool(jedisPoolConfig, redisConfig.getHost(), redisConfig.getPort(), redisConfig.getConnectionTimeout()
                , redisConfig.getSoTimeout(), redisConfig.getUsername(), redisConfig.getPassword(), redisConfig.getDatabase(), redisConfig.getName());
        if(testConnection()){
            log.info("{}连接成功！RedisServer HOST:{},PORT:{}",redisConfig.getName(),redisConfig.getHost(),redisConfig.getPort());
        }else {
            log.info("{}连接失败！RedisServer HOST:{},PORT:{}",redisConfig.getName(),redisConfig.getHost(),redisConfig.getPort());
        }
    }

    public boolean testConnection(){
        Jedis resource = null;
        try {
            resource = jedisPool.getResource();
            return resource.getConnection().isConnected() && resource.getConnection().ping();
        }catch (Exception e){
            return false;
        }finally {
            closeJedis(resource);
        }
    }

    public Jedis autoCloseClient(){
        return (Jedis) Enhancer.create(Jedis.class, new RedisProxyInterceptor(true));
    }

    public Jedis autoCloseNoThrowExClient(){
        return (Jedis) Enhancer.create(Jedis.class, new RedisProxyInterceptor(false));
    }

    public RedisUtil util(){
        return redisUtil;
    }

    public void destroy(){
        jedisPool.destroy();
    }

    private void closeJedis(Jedis jedis){
        try {
            if(jedis != null) {
                jedis.close();
            }
        }catch (Exception e){
            log.error(redisConfig.getName()+"将连接归还给连接池发生异常",e);
        }
    }

    class RedisProxyInterceptor implements MethodInterceptor {

        private boolean throwEx;

        public RedisProxyInterceptor(boolean throwEx){
            this.throwEx = throwEx;
        }

        @Override
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            Jedis resource = null;
            try {
                resource = jedisPool.getResource();
                return method.invoke(resource, objects);
            }catch (Exception e){
                if(throwEx){
                    throw e;
                }else {
                    log.error(redisConfig.getName()+"执行" + method.getName() + "方法时发生异常",e);
                    return null;
                }
            }finally {
                closeJedis(resource);
            }
        }
    }

    public class RedisUtil{

        public long hsetSerializeValue(String key,String field,Object value){
            Jedis resource = null;
            try {
                resource = jedisPool.getResource();
                return resource.hset(key.getBytes(StandardCharsets.ISO_8859_1),field.getBytes(StandardCharsets.ISO_8859_1),SerializeUtil.protostuffSerialize(value));
            }finally {
                closeJedis(resource);
            }
        }

        public <T> T hgetSerializeValue(String key,String field){
            Jedis resource = null;
            try {
                resource = jedisPool.getResource();
                byte[] bytes = resource.hget(key.getBytes(StandardCharsets.ISO_8859_1), field.getBytes(StandardCharsets.ISO_8859_1));
                if(bytes != null){
                    return SerializeUtil.protostuffDeserialize(bytes);
                }
                return null;
            }finally {
                closeJedis(resource);
            }
        }

        public String hmsetSerializeValue(String key, Map<String,?> map){
            Jedis resource = null;
            try {
                resource = jedisPool.getResource();
                Map<byte[],byte[]> valueMap = new HashMap<>();
                map.forEach((k,v)-> valueMap.put(k.getBytes(StandardCharsets.ISO_8859_1), SerializeUtil.protostuffSerialize(v)));
                return resource.hmset(key.getBytes(StandardCharsets.ISO_8859_1),valueMap);
            }finally {
                closeJedis(resource);
            }
        }

        public <T> HashMap<String,T> hgetAllSerializeValue(String key) {
            Jedis resource = null;
            try {
                resource = jedisPool.getResource();
                Map<byte[], byte[]> map = resource.hgetAll(key.getBytes(StandardCharsets.ISO_8859_1));
                if(map != null){
                    HashMap<String,T> result = new HashMap<>();
                    map.forEach((k, v) -> result.put(new String(k, StandardCharsets.ISO_8859_1), SerializeUtil.protostuffDeserialize(v)));
                    return result;
                }
                return null;
            }finally {
                closeJedis(resource);
            }
        }
    }

}
