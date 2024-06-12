package com.lightframework.auth.shiro.service.impl;

import com.lightframework.auth.core.model.LoginParam;
import com.lightframework.auth.common.model.UserInfo;
import com.lightframework.auth.core.service.AuthService;
import com.lightframework.auth.shiro.properties.ShiroAuthConfigProperties;
import com.lightframework.common.BusinessException;
import com.lightframework.util.verifycode.VerifyCode;
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
public class DefaultAuthServiceImpl implements AuthService {

    @Autowired
    private ShiroAuthConfigProperties authConfigProperties;

    @Override
    public UserInfo login(LoginParam loginParam) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        VerifyCode verifyCode = (VerifyCode) request.getSession().getAttribute(VerifyCodeUtil.VERIFY_CODE);
        if(authConfigProperties.getConfiguration().getVerifyCode().isEnableVerifyCode()){
            if(verifyCode == null){
                throw new BusinessException("验证码无效");
            }else if(loginParam.getVerifyCode() == null || loginParam.getVerifyCode().length() == 0){
                throw new BusinessException("请输入验证码");
            }else if(!verifyCode.getCode().equals(loginParam.getVerifyCode())){
                throw new BusinessException("验证码输入错误");
            }else if(verifyCode.isExpired()){
                throw new BusinessException("验证码已过期");
            }
        }
        UsernamePasswordToken token = new UsernamePasswordToken(loginParam.getUsername(), loginParam.getPassword());
        token.setRememberMe(loginParam.getRememberMe());
        Subject subject = SecurityUtils.getSubject();
        try {
            subject.login(token);
            UserInfo userInfo = (UserInfo) subject.getPrincipal();
            if(userInfo != null){
                if(!userInfo.isEnable()){
                    subject.logout();
                    throw new BusinessException("登录失败，用户已被禁用！");
                }
            }
            userInfo.setAccessToken(subject.getSession().getId().toString());
            return userInfo;
        }catch (AuthenticationException e){
            throw new BusinessException("用户名或密码不正确");
        }
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
        VerifyCode verifyCode = VerifyCodeUtil.createRandom(false,4,authConfigProperties.getConfiguration().getVerifyCode().getExpireTimeSecond());
        HttpSession httpSession = request.getSession();
        httpSession.setAttribute(VerifyCodeUtil.VERIFY_CODE,verifyCode);
        return verifyCode.getCode();
    }
}
