package com.lightframework.auth.shiro.controller;

import com.lightframework.auth.core.model.LoginParam;
import com.lightframework.common.BusinessException;
import com.lightframework.core.annotation.BusinessController;
import com.lightframework.util.verifycode.VerifyCodeUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/** 授权
 * @author yg
 * @date 2023/4/20 10:39
 * @version 1.0
 */
@BusinessController
@RequestMapping("/api/auth")
public class AuthController {

    @Value("${auth.verifyCode.enable:false}")
    private boolean enableVerifyCode;

    @PostMapping("/login")
    public Object login(@RequestBody LoginParam loginParam, HttpServletRequest request){
        String verifyCode = (String) request.getSession().getAttribute(VerifyCodeUtil.VERIFY_CODE);
        if(enableVerifyCode && (verifyCode == null || !verifyCode.equals(loginParam.getVerifyCode()))){
            throw new BusinessException("验证码输入错误");
        }
        UsernamePasswordToken token = new UsernamePasswordToken(loginParam.getUsername(), loginParam.getPassword());
        Subject subject = SecurityUtils.getSubject();
        try {
            subject.login(token);
        }catch (AuthenticationException e){
            throw new BusinessException("登录失败");
        }
        return "登录成功";
    }

    @GetMapping("/logout")
    public boolean logout() {
        Subject subject = SecurityUtils.getSubject();
        subject.logout();
        return true;
    }

}
