package com.lightframework.auth.jwt.properties;

import com.lightframework.auth.core.properties.AuthConfigProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/*** jwt配置
 * @author yg
 * @date 2024/5/24 9:23
 * @version 1.0
 */
@Component
@ConfigurationProperties(prefix = "auth")
@Getter
@Setter
public class JwtAuthConfigProperties extends AuthConfigProperties {

    private String tokenPrefix = "Bearer ";
}
