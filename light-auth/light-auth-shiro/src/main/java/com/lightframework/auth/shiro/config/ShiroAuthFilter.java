package com.lightframework.auth.shiro.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lightframework.common.BusinessResponse;
import com.lightframework.common.BusinessStatus;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.springframework.http.MediaType;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;


public class ShiroAuthFilter extends FormAuthenticationFilter {

    /**
     * 前后端分离项目，“重定向到登录页”改为“输出JSON”
     * @param request
     * @param response
     * @throws IOException
     */
    @Override
    protected void redirectToLogin(ServletRequest request, ServletResponse response) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(mapper.writeValueAsString(new BusinessResponse(BusinessStatus.UNAUTHORIZED)));
        response.getWriter().flush();
        response.getWriter().close();
    }

}
