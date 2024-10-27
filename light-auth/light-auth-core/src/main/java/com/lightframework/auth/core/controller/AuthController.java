package com.lightframework.auth.core.controller;

import com.lightframework.auth.core.model.LoginParam;
import com.lightframework.auth.common.model.UserInfo;
import com.lightframework.auth.core.service.AuthService;
import com.lightframework.common.BusinessType;
import com.lightframework.common.annotation.SystemLogger;
import com.lightframework.core.annotation.BusinessController;
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

    @PostMapping("/login")
    @SystemLogger(moduleKey = "AUTH", moduleName = "授权",operationDesc = "登录",businessType = BusinessType.LOGIN)
    public UserInfo login(@RequestBody LoginParam loginParam){
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
        VerifyCodeUtil.createVCodeImage(response,verifyCode);
    }

}
