package com.lightframework.auth.jwt.service.impl;

import com.lightframework.auth.common.model.UserInfo;
import com.lightframework.auth.common.service.UserInfoService;
import com.lightframework.util.spring.SpringServletUtil;
import org.springframework.security.core.Authentication;
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
        return (UserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
