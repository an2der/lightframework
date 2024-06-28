package com.lightframework.auth.shiro.properties;

import com.lightframework.auth.core.properties.AuthConfigProperties;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/*** shiro配置
 * @author yg
 * @date 2024/5/24 9:23
 * @version 1.0
 */
@Component
@ConfigurationProperties(prefix = "auth.shiro")
@Getter
@Setter
public class ShiroAuthConfigProperties {

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private AuthConfigProperties configuration;

    @Autowired
    public void configuration(AuthConfigProperties configuration) {
        this.configuration = configuration;
    }

    public AuthConfigProperties getConfiguration() {
        return configuration;
    }

    /**
     * 密码加密配置
     */
    private PasswordCrypto passwordCrypto = new PasswordCrypto();

    /**
     * 密码加密
     */
    @Getter
    @Setter
    public static class PasswordCrypto{
        /**
         * hash算法
         */
        private String hashAlgorithm = "MD5";

        /**
         * hash次数
         */
        private int hashIterations = 1;
    }
}
