package com.lightframework.util.spring;

import java.util.Map;

/*** Spring Bean 工具
 * @author yg
 * @date 2023/11/7 11:40
 * @version 1.0
 */
public class SpringBeanUtil {

    public static Object getBean(String beanName){
        if(SpringContextUtil.getContext() != null){
            return SpringContextUtil.getContext().getBean(beanName);
        }
        return null;
    }

    public static <T> T getBean(Class<T> type) {
        if(SpringContextUtil.getContext() != null){
            return SpringContextUtil.getContext().getBean(type);
        }
        return null;
    }

    public static <T> Map<String, T> getBeansByType(Class<T> clazz){
        if(SpringContextUtil.getContext() != null){
            return SpringContextUtil.getContext().getBeansOfType(clazz);
        }
        return null;
    }

}
