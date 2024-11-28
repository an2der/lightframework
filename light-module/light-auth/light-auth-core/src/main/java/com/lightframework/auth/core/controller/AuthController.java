package com.lightframework.auth.core.controller;

import com.lightframework.auth.core.model.LoginParam;
import com.lightframework.auth.common.model.UserInfo;
import com.lightframework.auth.core.properties.AuthConfigProperties;
import com.lightframework.auth.core.service.AuthService;
import com.lightframework.web.common.BusinessException;
import com.lightframework.web.common.BusinessType;
import com.lightframework.web.common.annotation.SystemLogger;
import com.lightframework.web.core.annotation.BusinessController;
import com.lightframework.util.verifycode.VerifyCode;
import com.lightframework.util.verifycode.VerifyCodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** 授权
 * @author yg
 * @date 2023/4/20 10:39
 * @version 1.0
 */
@BusinessController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    @Lazy
    private AuthService authService;

    @Autowired
    private AuthConfigProperties authConfigProperties;

    @PostMapping("/login")
    @SystemLogger(moduleKey = "AUTH", moduleName = "授权",operationDesc = "登录",businessType = BusinessType.LOGIN)
    public UserInfo login(@RequestBody LoginParam loginParam){
        if(authConfigProperties.getVerifyCode().isEnableVerifyCode()){
            validateCode(loginParam.getVerifyCode());
        }
        return authService.login(loginParam);
    }

    @GetMapping("/logout")
    @SystemLogger(moduleKey = "AUTH", moduleName = "授权",operationDesc = "登出",businessType = BusinessType.LOGOUT)
    public boolean logout() {
        return authService.logout();
    }

    @GetMapping("/verifyImage")
    public void fetchVerifyImage(HttpServletResponse response) throws IOException {
        String verifyCode = authService.generateVerifyCode();
        VerifyCodeUtil.writeImage(verifyCode,response);
    }
    private void validateCode(String code){
        VerifyCode verifyCode = authService.getVerifyCode();
        if(verifyCode == null){
            throw new BusinessException("验证码无效");
        }else if(code == null || code.length() == 0){
            throw new BusinessException("请输入验证码");
        }else if(!verifyCode.getCode().equals(code)){
            throw new BusinessException("验证码输入错误");
        }else if(verifyCode.isExpired()){
            throw new BusinessException("验证码已过期");
        }
    }
}
