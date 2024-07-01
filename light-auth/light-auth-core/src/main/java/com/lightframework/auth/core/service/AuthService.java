package com.lightframework.auth.core.service;

import com.lightframework.auth.core.model.LoginParam;
import com.lightframework.auth.common.model.UserInfo;
import com.lightframework.common.BusinessException;
import com.lightframework.util.verifycode.VerifyCode;

/***
 * @author yg
 * @date 2023/10/31 10:52
 * @version 1.0
 */
public abstract class AuthService {

    /**
     * 登录
     * @param loginParam 登录请求对象
     * @return session id
     */
    public abstract UserInfo login(LoginParam loginParam);

    /**
     * 登出
     * @return
     */
    public abstract boolean logout();

    /**
     * 生成验证码
     * @return 验证码
     */
    public abstract String generateVerifyCode();

    protected void validateCode(String code, VerifyCode verifyCode){
        if(verifyCode == null){
            throw new BusinessException("验证码无效");
        }else if(code == null || code.length() == 0){
            throw new BusinessException("请输入验证码");
        }else if(!verifyCode.getCode().equals(code)){
            throw new BusinessException("验证码输入错误");
        }else if(verifyCode.isExpired()){
            throw new BusinessException("验证码已过期");
        }
    }

}
