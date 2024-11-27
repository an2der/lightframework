package com.lightframework.util.serialize;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

public class ProtostuffUtil {

    private static final Schema<ObjectWrapper> schema = RuntimeSchema.getSchema(ObjectWrapper.class);

    private ProtostuffUtil(){}

    /**
     * 序列化方法，把指定对象序列化成字节数组
     *
     * @param obj
     * @return
     */
    public static byte[] serialize(Object obj) {
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            return ProtobufIOUtil.toByteArray(new ObjectWrapper(obj), schema, buffer);
        } finally {
            buffer.clear();
        }
    }

    /**
     * 反序列化方法，将字节数组反序列化成指定Class类型
     *
     * @param data
     * @param <T>
     * @return
     */
    public static <T> T deserialize(byte[] data) {
        ObjectWrapper<T> objectWrapper = new ObjectWrapper();
        ProtobufIOUtil.mergeFrom(data, objectWrapper, schema);
        return objectWrapper.object;
    }

    private static class ObjectWrapper<T>{

        T object;

        ObjectWrapper(){}

        ObjectWrapper(T object){
            this.object = object;
        }
    }

}
