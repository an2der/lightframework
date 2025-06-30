package com.lightframework.auth.jwt.service.impl;

import com.lightframework.auth.common.model.UserInfo;
import com.lightframework.auth.common.service.UserInfoService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/** 当前用户信息
 * @author yg
 * @date 2022/6/9 13:41
 * @version 1.0
 */
@Service
public class JwtUserInfoServiceImpl implements UserInfoService {

    @Override
    public UserInfo getUserInfo() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return (UserInfo) (principal instanceof UserInfo ? principal : null);
    }
}
