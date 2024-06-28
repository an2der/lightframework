package com.lightframework.auth.jwt.properties;

import com.lightframework.auth.core.properties.AuthConfigProperties;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/*** jwt配置
 * @author yg
 * @date 2024/5/24 9:23
 * @version 1.0
 */
@Component
@ConfigurationProperties(prefix = "auth.jwt")
@Getter
@Setter
public class JwtAuthConfigProperties {

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
     * token 前缀
     */
    private String tokenPrefix = "Bearer ";
}
