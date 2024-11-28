package com.lightframework.util.spring.web;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lightframework.common.LightException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SpringJacksonUtil {

    private static ObjectMapper objectMapper;

    @Autowired
    private void setObjectMapper(ObjectMapper objectMapper){
        SpringJacksonUtil.objectMapper = objectMapper;
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
