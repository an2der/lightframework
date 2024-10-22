package com.lightframework.auth.core.util;

import com.lightframework.auth.common.model.UserInfo;
import com.lightframework.auth.common.service.UserInfoService;
import com.lightframework.util.spring.SpringBeanUtil;

public class SecurityUtil {

    private SecurityUtil(){}

    public static UserInfo getUserInfo(){
        UserInfoService userInfoService = SpringBeanUtil.getBean(UserInfoService.class);
        return userInfoService != null?userInfoService.getUserInfo():null;
    }
}
