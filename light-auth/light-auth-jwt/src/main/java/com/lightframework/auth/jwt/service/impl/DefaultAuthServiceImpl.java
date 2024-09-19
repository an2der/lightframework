package com.lightframework.auth.jwt.service.impl;

import com.lightframework.auth.common.model.UserInfo;
import com.lightframework.auth.core.model.LoginParam;
import com.lightframework.auth.core.service.AuthService;
import com.lightframework.auth.jwt.model.JwtUserInfo;
import com.lightframework.auth.jwt.properties.JwtAuthConfigProperties;
import com.lightframework.auth.jwt.util.JwtTokenUtil;
import com.lightframework.common.BusinessException;
import com.lightframework.util.spring.SpringServletUtil;
import com.lightframework.util.verifycode.VerifyCode;
import com.lightframework.util.verifycode.VerifyCodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Service
public class DefaultAuthServiceImpl extends AuthService {

    @Autowired
    private JwtAuthConfigProperties authConfigProperties;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    public UserInfo login(LoginParam loginParam) {
        if(authConfigProperties.getConfiguration().getVerifyCode().isEnableVerifyCode()){
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            VerifyCode verifyCode = (VerifyCode) request.getSession().getAttribute(VerifyCodeUtil.VERIFY_CODE);
            validateCode(loginParam.getVerifyCode(),verifyCode);
        }
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(loginParam.getUsername(),loginParam.getPassword());
        try {
            Authentication authentication = authenticationManager.authenticate(token);
            if(authentication == null){
                throw new BusinessException("登录失败");
            }
            JwtUserInfo userInfo = (JwtUserInfo) authentication.getPrincipal();
            if(userInfo != null){
                if(!userInfo.isEnable()){
                    throw new BusinessException("登录失败，用户已被禁用！");
                }
                String accessToken = authConfigProperties.getTokenPrefix() + jwtTokenUtil.generateToken(userInfo);
                HttpServletResponse response = SpringServletUtil.getResponse();
                Cookie cookie = new Cookie(authConfigProperties.getConfiguration().getTokenKey(),accessToken);
                cookie.setMaxAge(authConfigProperties.getConfiguration().getExpireTimeMinute() > 0?
                        (int)(authConfigProperties.getConfiguration().getExpireTimeMinute() * 60):Integer.MAX_VALUE);
                cookie.setHttpOnly(true);
                cookie.setPath("/");
                response.addCookie(cookie);
                userInfo.setAccessToken(accessToken);
                userInfo.setPassword(null);
                return userInfo;
            }else {
                throw new BusinessException("用户不存在！");
            }
        }catch (BadCredentialsException e){
            throw new BusinessException("密码错误！");
        }

    }

    @Override
    public boolean logout() {
        HttpServletResponse response = SpringServletUtil.getResponse();
        Cookie cookie = new Cookie(authConfigProperties.getConfiguration().getTokenKey(),"");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);
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
