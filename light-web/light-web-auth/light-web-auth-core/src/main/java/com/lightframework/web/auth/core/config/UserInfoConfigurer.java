package com.lightframework.web.auth.core.config;

import com.lightframework.web.auth.core.resolver.UserInfoMethodArgumentAnnotationResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/** 核心配置类
 * @author yg
 * @date 2022/6/13 11:37
 * @version 1.0
 */
@Configuration
public class UserInfoConfigurer implements WebMvcConfigurer {

    @Autowired(required = false)
    private UserInfoMethodArgumentAnnotationResolver userinfoMethodArgumentAnnotationResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        if(userinfoMethodArgumentAnnotationResolver != null) {
            resolvers.add(userinfoMethodArgumentAnnotationResolver);
        }
    }
}
