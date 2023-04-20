package com.lightframework.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lightframework.core.handler.BusinessReturnValueHandler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.text.SimpleDateFormat;
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

    @Autowired
    private JacksonProperties jacksonProperties;

    @Override
    public void afterPropertiesSet() {
        ObjectMapper mapper = new ObjectMapper();
        if(jacksonProperties.getDateFormat() != null){
            mapper.setDateFormat(new SimpleDateFormat(jacksonProperties.getDateFormat()));
        }
        if(jacksonProperties.getTimeZone() != null){
            mapper.setTimeZone(jacksonProperties.getTimeZone());
        }
        List<HandlerMethodReturnValueHandler> returnValueHandlers = new ArrayList<>();
        returnValueHandlers.add(new BusinessReturnValueHandler(mapper));
        returnValueHandlers.addAll(requestMappingHandlerAdapter.getReturnValueHandlers());
        requestMappingHandlerAdapter.setReturnValueHandlers(returnValueHandlers);
    }
}
