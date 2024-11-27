package com.lightframework.util.serialize;

import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;

public class SerializeUtil {

    private SerializeUtil(){}

    public static <T> byte[] protostuffSerialize(T obj){
        return ProtostuffUtil.serialize(obj);
    }

    public static <T> T protostuffDeserialize(byte[] data) {
        return ProtostuffUtil.deserialize(data);
    }

    public static byte[] javaSerialize(Serializable obj){
        return SerializationUtils.serialize(obj);
    }

    public static <T> T javaDeserialize(byte[] data){
        return SerializationUtils.deserialize(data);
    }
}
