package com.lightframework.auth.jwt.service.impl;

import com.lightframework.auth.common.model.UserInfo;
import com.lightframework.auth.core.service.UserAuthService;
import com.lightframework.auth.jwt.model.JwtUserInfo;
import com.lightframework.web.common.BusinessException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/***
 * @author yg
 * @date 2024/5/23 20:14
 * @version 1.0
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserAuthService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserInfo userInfo = userService.getUserInfoByUsername(username);
        if(userInfo == null){
            throw new BusinessException("用户不存在");
        }
        JwtUserInfo jwtUserInfo = new JwtUserInfo();
        BeanUtils.copyProperties(userInfo,jwtUserInfo);
        jwtUserInfo.setPassword(userInfo.password());
        return jwtUserInfo;
    }
}
