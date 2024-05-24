package com.lightframework.auth.shiro.crypto;

import com.lightframework.auth.shiro.properties.ShiroAuthConfigProperties;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/***
 * @author yg
 * @date 2023/11/7 10:29
 * @version 1.0
 */
@Component
public class ShiroCrypto {

    @Autowired
    private ShiroAuthConfigProperties authConfigProperties;

    /**
     * 通过MD5加密密码
     * @param password 密码
     * @param salt 盐
     * @return
     */
    public String encryptPassword(String password,String salt){
        return new SimpleHash(authConfigProperties.getPasswordCrypto().getHashAlgorithm(), password, salt, authConfigProperties.getPasswordCrypto().getHashIterations()).toString();
    }

    public String encryptPassword(String password){
        return this.encryptPassword(password, authConfigProperties.getSecret());
    }
}
