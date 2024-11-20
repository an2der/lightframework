package com.lightframework.auth.core.util;

import com.lightframework.auth.common.model.UserInfo;
import com.lightframework.auth.common.service.UserInfoService;
import com.lightframework.util.spring.SpringContextUtil;

public class SecurityUtil {

    private static UserInfoService userInfoService;

    private SecurityUtil(){}

    public static UserInfo getUserInfo(){
        if(userInfoService == null) {
            userInfoService = SpringContextUtil.getBean(UserInfoService.class);
        }
        return userInfoService != null?userInfoService.getUserInfo():null;
    }
}
