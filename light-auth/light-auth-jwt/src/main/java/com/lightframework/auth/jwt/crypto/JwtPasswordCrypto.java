package com.lightframework.auth.jwt.crypto;

import com.lightframework.auth.core.crypto.PasswordCrypto;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/***
 * @author yg
 * @date 2023/11/7 10:29
 * @version 1.0
 */
@Component
public class JwtPasswordCrypto implements PasswordCrypto {

    @Override
    public String encryptPassword(String password, String salt) {
        return encryptPassword(password);
    }

    @Override
    public String encryptPassword(String password){
        return new BCryptPasswordEncoder().encode(password);
    }
}
