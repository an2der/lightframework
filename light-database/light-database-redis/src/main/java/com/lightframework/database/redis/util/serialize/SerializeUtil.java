package com.lightframework.database.redis.util.serialize;

/**
 * @desc 对象序列化/反序列化工具类
 * @create 2019-02-25 23:29
 **/
public class SerializeUtil {
    /*
     * 序列化
     * */
    public static <T> byte[] serialize(T t) {
        return ProtostuffUtil.serializer(t);
    }
    /*
     * 反序列化
     * */
    public static <T> T deserialize(byte[] bytes, Class<T> clazz) {
        return ProtostuffUtil.deserializer(bytes, clazz);
    }

}

