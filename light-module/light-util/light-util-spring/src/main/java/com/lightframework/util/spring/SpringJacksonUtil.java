package com.lightframework.util.spring;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.lightframework.util.json.JacksonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SpringJacksonUtil extends JacksonUtil {

    @Autowired
    private void setObjectMapper(ObjectMapper objectMapper){
        SpringJacksonUtil.objectMapper = objectMapper;
    }

}
