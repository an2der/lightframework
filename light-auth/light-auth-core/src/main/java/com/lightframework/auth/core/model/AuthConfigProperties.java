package com.lightframework.auth.core.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/** 授权配置对象
 * @author yg
 * @date 2023/5/31 14:38
 * @version 1.0
 */
@Component
@ConfigurationProperties(prefix = "auth")
@Getter
@Setter
public class AuthConfigProperties {

    private InterceptUrl interceptUrl;

    private Integer expireTimeMinute; //分钟

    private boolean enableVerifyCode = false;

    private PasswordCrypto passwordCrypto;


    @Getter
    @Setter
    public class InterceptUrl{
        private List<String> includes;

        private List<String> excludes;
    }

    @Getter
    @Setter
    public class PasswordCrypto{
        private String hashAlgorithm = "MD5";

        private int hashIterations = 1;
    }
}
