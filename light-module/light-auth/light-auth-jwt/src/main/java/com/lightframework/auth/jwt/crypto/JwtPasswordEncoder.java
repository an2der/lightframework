package com.lightframework.auth.jwt.crypto;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.digest.Digester;
import com.lightframework.auth.jwt.properties.JwtAuthConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@ConditionalOnProperty(prefix = "auth.jwt.password-crypto",name = "hash-algorithm")
public class JwtPasswordEncoder implements PasswordEncoder {
    @Autowired
    private JwtAuthConfigProperties properties;

    private Digester digester;

    @PostConstruct
    private void init(){
        digester = DigestUtil.digester(properties.getPasswordCrypto().getHashAlgorithm());
    }
    @Override
    public String encode(CharSequence rawPassword) {
        return digester.digestHex(rawPassword.toString());
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return encode(rawPassword).equalsIgnoreCase(encodedPassword);
    }
}
