package com.lightframework.auth.jwt.handler;

import com.lightframework.web.common.BusinessResponse;
import com.lightframework.web.common.BusinessStatus;
import com.lightframework.util.spring.web.SpringServletUtil;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/***
 * @author yg
 * @date 2024/7/1 11:03
 * @version 1.0
 */
public class JwtAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        SpringServletUtil.responseJSONStr(response,new BusinessResponse(BusinessStatus.FORBIDDEN));
    }
}
