package com.lightframework.util.reflect;

import com.lightframework.common.LightException;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Author yg
 * @Date 2025/9/23 15:46
 */
public class ReflectionUtil {

    private ReflectionUtil(){}

    public static <T> Set<Class<? extends T>> scanSubTypes(String packageName,Class<T> type){
        Reflections reflections = new Reflections(packageName);
        return reflections.getSubTypesOf(type);
    }
    public static <T> List<T> scanSubTypesToObjectList(String packageName,Class<T> type){
        List<T> result = new ArrayList<>();
        Set<Class<? extends T>> subTypes = scanSubTypes(packageName, type);
        for (Class<? extends T> subType : subTypes) {
            try {
                result.add(subType.newInstance());
            } catch (Exception e) {
                throw new LightException(e);
            }
        }
        return result;
    }
}
