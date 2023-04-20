package com.lightframework.auth.core.model;

/** 登录请求对象
 * @author yg
 * @date 2023/4/20 10:42
 * @version 1.0
 */
public class LoginParam {

    private String username;

    private String password;

    private String verifyCode;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getVerifyCode() {
        return verifyCode;
    }

    public void setVerifyCode(String verifyCode) {
        this.verifyCode = verifyCode;
    }
}
