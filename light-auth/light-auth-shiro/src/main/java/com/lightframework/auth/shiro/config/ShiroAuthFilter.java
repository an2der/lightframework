package com.lightframework.auth.shiro.config;

import com.lightframework.auth.core.helper.AuthHelper;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;


public class ShiroAuthFilter extends FormAuthenticationFilter {

    /**
     * 前后端分离项目，“重定向到登录页”改为“输出JSON”
     * @param request
     * @param response
     * @throws IOException
     */
    @Override
    protected void redirectToLogin(ServletRequest request, ServletResponse response) throws IOException {
        AuthHelper.responseUnauthorized(response);
    }

}
