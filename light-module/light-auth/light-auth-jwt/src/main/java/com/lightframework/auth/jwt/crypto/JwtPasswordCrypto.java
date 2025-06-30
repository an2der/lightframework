package com.lightframework.auth.jwt.crypto;

import com.lightframework.auth.core.crypto.PasswordCrypto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/***
 * @author yg
 * @date 2023/11/7 10:29
 * @version 1.0
 */
@Component
public class JwtPasswordCrypto implements PasswordCrypto {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public String encryptPassword(String password, String salt) {
        return encryptPassword(password);
    }

    @Override
    public String encryptPassword(String password){
        return passwordEncoder.encode(password);
    }
}
