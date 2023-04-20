package com.lightframework.auth.core.controller;

import com.lightframework.core.annotation.BusinessController;
import com.lightframework.util.verifycode.VerifyCodeUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/** 授权验证码
 * @author yg
 * @date 2023/4/20 10:39
 * @version 1.0
 */
@BusinessController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/verifyImage")
    public void fetchVerifyImage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String verifyCode = VerifyCodeUtil.createRandom(false,4);
        HttpSession httpSession = request.getSession();
        httpSession.setAttribute(VerifyCodeUtil.VERIFY_CODE,verifyCode);
        VerifyCodeUtil.createVCodeImage(response,verifyCode);
    }
}
