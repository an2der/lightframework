package com.lightframework.auth.core.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** 授权配置对象
 * @author yg
 * @date 2023/5/31 14:38
 * @version 1.0
 */
@Component
@ConfigurationProperties("auth")
@Getter
@Setter
public class AuthConfigProperties {

    private String tokenKey = "Authorization";

    private String secret = "LIGHT123456789ABCDEF";

    private int expireTimeMinute = 43200; //分钟

    private InterceptUrl interceptUrl = new InterceptUrl();

    private VerifyCode verifyCode = new VerifyCode();

    /**
     * 拦截地址
     */
    @Getter
    @Setter
    public static class InterceptUrl{
        private List<String> includes = Arrays.asList("/**");

        private List<String> excludes;
    }


    /**
     * 验证码设置
     */
    @Getter
    @Setter
    public static class VerifyCode{
        private boolean enableVerifyCode = false;

        private int expireTimeSecond = 0; //过期时间：秒

    }

}
