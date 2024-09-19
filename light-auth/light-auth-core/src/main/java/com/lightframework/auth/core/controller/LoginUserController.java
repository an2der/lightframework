package com.lightframework.auth.core.controller;

import com.lightframework.auth.common.model.UserInfo;
import com.lightframework.core.annotation.BusinessController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@BusinessController
@RequestMapping("/api/user")
public class LoginUserController {

    @GetMapping("/userinfo")
    public UserInfo userinfo(UserInfo userInfo){
        return userInfo;
    }
}
