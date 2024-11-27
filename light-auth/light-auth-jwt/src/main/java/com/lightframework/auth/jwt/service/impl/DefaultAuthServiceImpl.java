package com.lightframework.auth.jwt.service.impl;

import com.lightframework.auth.common.model.UserInfo;
import com.lightframework.auth.core.model.LoginParam;
import com.lightframework.auth.core.service.AuthService;
import com.lightframework.auth.jwt.properties.JwtAuthConfigProperties;
import com.lightframework.auth.jwt.util.JwtTokenUtil;
import com.lightframework.common.BusinessException;
import com.lightframework.util.serialize.SerializeUtil;
import com.lightframework.util.spring.web.SpringServletUtil;
import com.lightframework.util.verifycode.VerifyCode;
import com.lightframework.util.verifycode.VerifyCodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

@Service
@ConditionalOnMissingBean(AuthService.class)
public class DefaultAuthServiceImpl extends AuthService {

    @Autowired
    private JwtAuthConfigProperties authConfigProperties;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    public UserInfo login(LoginParam loginParam) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(loginParam.getUsername(),loginParam.getPassword());
        try {
            Authentication authentication = authenticationManager.authenticate(token);
            if(authentication == null){
                throw new BusinessException("登录失败");
            }
            UserInfo userInfo = (UserInfo) authentication.getPrincipal();
            if(userInfo != null){
                if(!userInfo.isEnabled()){
                    throw new BusinessException("登录失败，用户已被禁用！");
                }
                userInfo.setPassword(null);
                String accessToken = authConfigProperties.getTokenPrefix() + jwtTokenUtil.generateToken(userInfo);
                HttpServletResponse response = SpringServletUtil.getResponse();
                Cookie cookie = new Cookie(authConfigProperties.getConfiguration().getTokenKey(),accessToken);
                cookie.setMaxAge(authConfigProperties.getConfiguration().getExpireTimeMinute() > 0?
                        (int)(authConfigProperties.getConfiguration().getExpireTimeMinute() * 60):Integer.MAX_VALUE);
                cookie.setHttpOnly(true);
                cookie.setPath("/");
                response.addCookie(cookie);
                userInfo.setAccessToken(accessToken);
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
        VerifyCode verifyCode = VerifyCodeUtil.createRandom(false,4,authConfigProperties.getConfiguration().getVerifyCode().getExpireTimeSecond());
        HttpServletResponse response = SpringServletUtil.getResponse();
        Cookie cookie = new Cookie(VerifyCodeUtil.VERIFY_CODE,new String(SerializeUtil.protostuffSerialize(verifyCode), StandardCharsets.ISO_8859_1));
        cookie.setMaxAge(authConfigProperties.getConfiguration().getVerifyCode().getExpireTimeSecond() > 0?
                authConfigProperties.getConfiguration().getVerifyCode().getExpireTimeSecond():Integer.MAX_VALUE);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);
        return verifyCode.getCode();
    }

    @Override
    public VerifyCode getVerifyCode() {
        String code = SpringServletUtil.getCookie(VerifyCodeUtil.VERIFY_CODE);
        return code == null?null:SerializeUtil.protostuffDeserialize(code.getBytes(StandardCharsets.ISO_8859_1));
    }
}
