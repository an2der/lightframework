package com.lightframework.auth.shiro.service.impl;

import com.lightframework.auth.core.model.AuthConfigProperties;
import com.lightframework.auth.core.model.LoginParam;
import com.lightframework.auth.core.service.AuthService;
import com.lightframework.common.BusinessException;
import com.lightframework.util.verifycode.VerifyCodeUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthConfigProperties authConfigProperties;

    @Override
    public String login(LoginParam loginParam) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String verifyCode = (String) request.getSession().getAttribute(VerifyCodeUtil.VERIFY_CODE);
        if(authConfigProperties.isEnableVerifyCode() && (verifyCode == null || !verifyCode.equals(loginParam.getVerifyCode()))){
            throw new BusinessException("验证码输入错误");
        }
        UsernamePasswordToken token = new UsernamePasswordToken(loginParam.getUsername(), loginParam.getPassword());
        Subject subject = SecurityUtils.getSubject();
        try {
            subject.login(token);
        }catch (AuthenticationException e){
            throw new BusinessException("用户名或密码不正确");
        }
        return subject.getSession().getId().toString();
    }

    @Override
    public boolean logout() {
        Subject subject = SecurityUtils.getSubject();
        subject.logout();
        return true;
    }

    @Override
    public String generateVerifyCode() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String verifyCode = VerifyCodeUtil.createRandom(false,4);
        HttpSession httpSession = request.getSession();
        httpSession.setAttribute(VerifyCodeUtil.VERIFY_CODE,verifyCode);
        return verifyCode;
    }
}
