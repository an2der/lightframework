package com.lightframework.core.config;

import com.lightframework.core.handler.BusinessReturnValueHandler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.ArrayList;
import java.util.List;

/** 将自定义处理程序放到第一位
 * @author yg
 * @date 2022/7/1 17:57
 * @version 1.0
 */
@Configuration
public class BusinessReturnValueConfigurer implements InitializingBean {

    @Autowired
    private RequestMappingHandlerAdapter requestMappingHandlerAdapter;

    @Override
    public void afterPropertiesSet() {
        List<HandlerMethodReturnValueHandler> returnValueHandlers = new ArrayList<>();
        returnValueHandlers.add(new BusinessReturnValueHandler());
        returnValueHandlers.addAll(requestMappingHandlerAdapter.getReturnValueHandlers());
        requestMappingHandlerAdapter.setReturnValueHandlers(returnValueHandlers);
    }
}
