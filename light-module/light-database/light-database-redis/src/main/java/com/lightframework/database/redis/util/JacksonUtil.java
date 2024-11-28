package com.lightframework.database.redis.util;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;

/**
 * Jackson 转换json工具类
 * @Author hua,ma
 * @Date 2022/6/13
 **/
@Slf4j
public class JacksonUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //默认策略，无论如何都会序列化该字段，写不写效果都一样；
        mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        //取消日期格式[2022,4,2]
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,false);
        //格式化日期
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS"));
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        mapper.registerModule(javaTimeModule);
        //启动默认类型"@class": "cn.com.yl.framework.storage.redis.entity.Msg",
//        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        //反序列化时,遇到未知属性会不会报错 //true - 遇到没有的属性就报错 false - 没有的属性不会管，不会报错
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * 把对象转为字节数组
     * @param source 对象
     * @return 字节数组
     */
    public static byte[] serialize(Object source){
        byte[] bytes = null;
        try {
            bytes = mapper.writeValueAsBytes(source);
        } catch (JsonProcessingException e) {
            log.error("把对象转为字节数组 异常 " + source, e);
        }
        return bytes;
    }

    /**
     * 把对象转为json字符串
     * @param source
     * @return
     */
    public static String toJSON(Object source){
        String str = null;
        try {
            str = mapper.writeValueAsString(source);
        } catch (JsonProcessingException e) {
            log.error("把对象转为json字符串 异常 " + source, e);
        }
        return str;
    }

    /** 漂亮的格式化显示
     * 把对象转为json字符串
     * @param source
     * @return
     */
    public static String toJSONPretty(Object source){
        String str = null;
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(source);
        } catch (JsonProcessingException e) {
            log.error("把对象转为json字符串 异常 " + source, e);
        }
        return str;
    }

    /**
     * 把字节数组 转为对象
     * 反操作 {@link #serialize(Object)}
     * @param bytes 字节数组
     * @param type 对象类型
     * @return
     */
    public static <T> T deserialize(byte[] bytes, Class<T> type){
        T t = null;
        try {
            t = mapper.readValue(bytes, type);
        } catch (Exception e) {
            log.error("把字节数组 转为对象 异常", e);
        }
        return t;
    }
    /**
     * 把字节数组 转为 复杂泛型对象
     * 反操作 {@link #serialize(Object)}
     * @param bytes 字节数组
     * @param typeReference 泛型类型,例如 new TypeReference<List<RAlarm>>(){};     new TypeReference<Map<String, Object>>(){}
     * @return
     */
    public static <T>T deserialize(byte[] bytes, TypeReference<T> typeReference){
        T t = null;
        try {
            t = mapper.readValue(bytes, typeReference);
        } catch (Exception e) {
            log.error("把字节数组 转为对象 异常", e);
        }
        return t;
    }

    /**
     * 把字符串 转为单个对象
     * 反操作 {@link #toJSON(Object)}
     * @param json 字符串
     * @param type 对象类型
     * @return
     */
    public static <T>T parseObject(String json, Class<T> type) {
        T t = null;
        try {
            t = mapper.readValue(json, type);
        } catch (Exception e) {
            log.error("把字符串 转为单个对象 异常 " + json, e);
        }
        return t;
    }
    /**
     * 把字符串 转为 复杂结构的泛型类型对象
     * 反操作 {@link #toJSON(Object)}
     * @param json 字符串
     * @param typeReference  泛型类型 例如 List,Map
     * @return
     */
    public static <T> T parseObject(String json, TypeReference<T> typeReference) {
        T t = null;
        try {
            return mapper.readValue(json, typeReference);
        } catch (Exception e) {
            log.error("把字符串 转为单个对象 异常 " + json, e);
        }
        return t;
    }
}
