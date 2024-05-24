package com.lightframework.auth.shiro.config;

import com.lightframework.auth.shiro.properties.ShiroAuthConfigProperties;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.Serializable;

/**
 * 处理不支持cookie的环境
 */
public class WebSessionManager extends DefaultWebSessionManager {

    private ShiroAuthConfigProperties authConfigProperties;

    private static final String REFERENCED_SESSION_ID_SOURCE = "Stateless request";

    public WebSessionManager(ShiroAuthConfigProperties authConfigProperties) {
        this.authConfigProperties = authConfigProperties;
    }

    @Override
    protected Serializable getSessionId(ServletRequest request, ServletResponse response) {
        String sessionId = WebUtils.toHttp(request).getHeader(authConfigProperties.getTokenKey());
        //如果请求头中有 token 则其值为sessionId
        if (sessionId != null && sessionId.length() > 0) {
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_SOURCE, REFERENCED_SESSION_ID_SOURCE);
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID, sessionId);
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_IS_VALID, Boolean.TRUE);
            return sessionId;
        } else { //header中不存在token 按照父级的方式在cookie中获取
            return super.getSessionId(request, response);
        }
    }

}

