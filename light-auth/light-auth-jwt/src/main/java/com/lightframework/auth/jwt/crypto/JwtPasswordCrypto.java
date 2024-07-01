package com.lightframework.auth.jwt.crypto;

import com.lightframework.auth.core.crypto.PasswordCrypto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/***
 * @author yg
 * @date 2023/11/7 10:29
 * @version 1.0
 */
@Component
public class JwtPasswordCrypto implements PasswordCrypto {

    @Autowired
    private JwtPasswordCrypto authConfigProperties;

    @Override
    public String encryptPassword(String password){
        return new BCryptPasswordEncoder().encode(password);
    }
}
