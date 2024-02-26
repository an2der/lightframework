package com.lightframework.auth.core.service;

import com.lightframework.auth.core.model.LoginParam;
import com.lightframework.auth.core.model.UserInfo;

/***
 * @author yg
 * @date 2023/10/31 10:52
 * @version 1.0
 */
public interface AuthService {

    /**
     * 登录
     * @param loginParam 登录请求对象
     * @return session id
     */
    UserInfo login(LoginParam loginParam);

    /**
     * 登出
     * @return
     */
    boolean logout();

    /**
     * 生成验证码
     * @return 验证码
     */
    String generateVerifyCode();
}
