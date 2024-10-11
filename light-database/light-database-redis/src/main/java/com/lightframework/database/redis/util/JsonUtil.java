package com.lightframework.database.redis.util;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/** json工具类 (Gson)
 * @author ：mashuai
 * @date ：2022/04/21 9:28
 */
@Slf4j
@SuppressWarnings("unchecked")
public class JsonUtil {

    //private static final Gson gson = new GsonBuilder().create();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    /*
        localFormat：yyyy-M-d H:mm:ss
        enUsFormat：MMM d, yyyy h:mm:ss a
        iso8601Format：yyyy-MM-dd'T'HH:mm:ss'Z'
     */


    /**
     * 返回 gson 对象
     *
     * @return
     */
    public static Gson gson() {
        return gson;
    }

    /**
     * 判断字符串是否是json格式
     *
     * @param msg
     * @return
     */
    public static Boolean isJson(String msg) {
        try {
            JsonObject jsonObject = gson.fromJson(msg, JsonObject.class);
            log.debug("jsonObject= {}", jsonObject);
            return true;//是
        } catch (Exception e) {
            return false;//否
        }
    }

    /**
     * JsonObject可以是JsonObject，JsonArray，JsonPrimitive或JsonNull.
     *
     * @param msg
     * @return
     */
    public static JsonObject jsonObject(String msg) {
        JsonObject jsonObject = null;
        try {
            jsonObject = gson.fromJson(msg, JsonObject.class);
        } catch (Exception e) {
            log.error("JacksonUtil fromJson 异常", e);
        }
        return jsonObject;
    }

    public static JsonArray jsonArray(String msg) {
        JsonArray jsonArray = null;
        try {
            jsonArray = gson.fromJson(msg, JsonArray.class);
        } catch (Exception e) {
            log.error("JacksonUtil fromJson 异常", e);
        }
        return jsonArray;
    }

    public static String toJson(Object object) {
        String json = null;
        try {
            json = gson.toJson(object);
        } catch (Exception e) {
            log.error("JacksonUtil toJson 异常", e);
        }
        return json;
    }

    public static JsonObject map2JsonObject(Map<String, Object> map) {
        JsonObject jsonObject = null;
        try {
            jsonObject = jsonObject(map2json(map));
        } catch (Exception e) {
            log.error("map2JsonObject 异常", e);
        }

        return jsonObject;
    }

    public static Map<String, Object> json2map(String json) {
        Map<String, Object> map = null;
        try {
            map = gson.fromJson(json, Map.class);
        } catch (Exception e) {
            log.error("json2map 异常", e);
        }
        return map;
    }

    public static String map2json(Map<String, Object> map) {
        String json = null;
        try {
            json = gson.toJson(map);
        } catch (Exception e) {
            log.error("map2json 异常", e);
        }
        return json;
    }

    public static <T> T map2Object(Map<String, Object> map, Class<T> clazz) {
        T object = null;
        try {
            object = gson.fromJson(map2json(map), clazz);
        } catch (Exception e) {
            log.error("map2Object 异常", e);
        }

        return object;
    }
}