package com.lightframework.auth.jwt.handler;

import com.lightframework.common.BusinessResponse;
import com.lightframework.common.BusinessStatus;
import com.lightframework.util.spring.web.SpringServletUtil;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 认证失败处理
 */
public class JwtAuthenticationHandler implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        SpringServletUtil.responseJSONStr(response,new BusinessResponse(BusinessStatus.UNAUTHORIZED));
    }
}
