package com.lightframework.util.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.lightframework.common.LightException;

import java.io.IOException;
import java.text.SimpleDateFormat;

public class JacksonUtil {

    protected static ObjectMapper objectMapper;

    static {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,false);
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        //设置反序列化时JSON字符串中的字段在目标JAVA类中不存不会抛出异常
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper = mapper;
    }

    public static String serialize(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new LightException(e);
        }
    }

    public static byte[] serializeToBytes(Object source) {
        try {
            return objectMapper.writeValueAsBytes(source);
        } catch (JsonProcessingException e) {
            throw new LightException(e);
        }
    }

    public static <T> T deserialize(String json, Class<T> cla) {
        try {
            return objectMapper.readValue(json, cla);
        } catch (JsonProcessingException e) {
            throw new LightException(e);
        }
    }


    public static <T> T deserializeFromBytes(byte[] bytes, Class<T> type) {
        try {
            return objectMapper.readValue(bytes, type);
        } catch (IOException e) {
            throw new LightException(e);
        }
    }


    public static <T> T deserialize(String json, TypeReference<T> cla) {
        try {
            return objectMapper.readValue(json, cla);
        } catch (JsonProcessingException e) {
            throw new LightException(e);
        }
    }

    public static <T> T deserializeFromBytes(byte[] bytes, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(bytes, typeReference);
        } catch (IOException e) {
            throw new LightException(e);
        }
    }


    public static JsonNode readTree(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (IOException e) {
            throw new LightException(e);
        }
    }
}
