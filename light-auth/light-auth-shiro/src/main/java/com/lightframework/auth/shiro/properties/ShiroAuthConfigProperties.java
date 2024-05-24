package com.lightframework.auth.shiro.properties;

import com.lightframework.auth.core.properties.AuthConfigProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/*** shiro配置
 * @author yg
 * @date 2024/5/24 9:23
 * @version 1.0
 */
@Component
@ConfigurationProperties(prefix = "auth")
@Getter
@Setter
public class ShiroAuthConfigProperties extends AuthConfigProperties {

    private PasswordCrypto passwordCrypto = new PasswordCrypto();

    /**
     * 密码加密
     */
    @Getter
    @Setter
    public static class PasswordCrypto{
        private String hashAlgorithm = "MD5";

        private int hashIterations = 1;
    }
}
