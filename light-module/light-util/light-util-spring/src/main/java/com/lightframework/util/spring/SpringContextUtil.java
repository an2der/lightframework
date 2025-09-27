package com.lightframework.util.spring;

import cn.hutool.extra.spring.SpringUtil;
import com.lightframework.common.LightException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/*** Spring Context 工具
 * @author yg
 * @date 2023/11/7 11:40
 * @version 1.0
 */
public class SpringContextUtil extends SpringUtil {

    private SpringContextUtil(){}

    public static void exit(){
        SpringApplication.exit(getApplicationContext(), () -> 0);
        System.exit(0);
    }

    public static <T> Set<Class<? extends T>> scanSubTypes(String packageName, Class<T> type){
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(type));
        Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents(packageName);
        Set<Class<? extends T>> subTypes = new HashSet<>();
        for (BeanDefinition candidateComponent : candidateComponents) {
            try {
                Class<?> aClass = Class.forName(candidateComponent.getBeanClassName());
                if (aClass.equals(type)) {
                    continue;
                }
                subTypes.add((Class<? extends T>) aClass);
            } catch (Exception e) {
                throw new LightException(e);
            }
        }
        return subTypes;
    }
    public static <T> List<T> scanSubTypesToObjectList(String packageName, Class<T> type){
        Set<Class<? extends T>> classes = scanSubTypes(packageName, type);
        List<T> objects = new ArrayList<>();
        for (Class<? extends T> clazz : classes) {
            try {
                objects.add(clazz.newInstance());
            } catch (Exception e) {
                throw new LightException(e);
            }
        }
        return objects;
    }
}
