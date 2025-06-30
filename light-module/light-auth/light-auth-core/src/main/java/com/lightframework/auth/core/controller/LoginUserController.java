package com.lightframework.auth.core.controller;

import com.lightframework.auth.common.model.UserInfo;
import com.lightframework.web.common.BusinessType;
import com.lightframework.web.common.annotation.SystemLogger;
import com.lightframework.web.core.annotation.BusinessController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@BusinessController
@RequestMapping("/api/user")
public class LoginUserController {

    @GetMapping("/userinfo")
    @SystemLogger(moduleKey = "USER", moduleName = "用户",operationDesc = "获取登录用户信息",businessType = BusinessType.SELECT)
    public UserInfo userinfo(UserInfo userInfo){
        return userInfo;
    }
}
