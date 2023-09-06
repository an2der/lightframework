package com.lightframework.auth.shiro.service.impl;

import com.lightframework.auth.core.model.UserInfo;
import com.lightframework.auth.core.service.UserInfoService;
import org.apache.shiro.SecurityUtils;
import org.springframework.stereotype.Service;

/** 当前用户信息
 * @author yg
 * @date 2022/6/9 13:41
 * @version 1.0
 */
@Service
public class ShiroUserInfoServiceImpl implements UserInfoService {

    @Override
    public UserInfo getUserInfo() {
        return (UserInfo) SecurityUtils.getSubject().getPrincipal();
    }
}
