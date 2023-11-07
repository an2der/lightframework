package com.lightframework.util.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/*** Spring Bean 工具
 * @author yg
 * @date 2023/11/7 11:40
 * @version 1.0
 */
@Component
public class SpringBeanUtil implements ApplicationContextAware {

    private static ApplicationContext context = null;

    public static Object getBean(String beanName){
        if(context != null){
            return context.getBean(beanName);
        }
        return null;
    }

    public static <T> T getBean(Class<T> type) {
        if(context != null){
            return context.getBean(type);
        }
        return null;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
