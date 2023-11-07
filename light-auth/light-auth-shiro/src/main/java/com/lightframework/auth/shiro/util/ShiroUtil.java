package com.lightframework.auth.shiro.util;

import org.apache.shiro.crypto.hash.SimpleHash;

/***
 * @author yg
 * @date 2023/11/7 10:29
 * @version 1.0
 */
public class ShiroUtil {

    /**
     * 通过MD5加密密码
     * @param password 密码
     * @param salt 盐
     * @param hashIterations 加密次数
     * @return
     */
    public static String encryptPasswordByMD5(String password,String salt,int hashIterations){
        return new SimpleHash("MD5", password, salt, hashIterations).toString();
    }
}
