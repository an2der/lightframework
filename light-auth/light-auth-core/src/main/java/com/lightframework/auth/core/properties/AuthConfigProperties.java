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

    /**
     * token 键
     */
    private String tokenKey = "Authorization";

    /**
     * 秘钥
     */
    private String secret = "LIGHT123456789ABCDEF";

    /**
     * token过期时间 单位：分钟
     */
    private long expireTimeMinute = 43200; //分钟

    /**
     * 拦截地址设置
     */
    private InterceptUrl interceptUrl = new InterceptUrl();

    /**
     * 验证码设置
     */
    private VerifyCode verifyCode = new VerifyCode();

    /**
     * 拦截地址
     */
    @Getter
    @Setter
    public static class InterceptUrl{
        /**
         * 被拦截的地址
         */
        private List<String> includes = Arrays.asList("/**");

        /**
         * 排除的拦截地址
         */
        private List<String> excludes;
    }


    /**
     * 验证码设置
     */
    @Getter
    @Setter
    public static class VerifyCode{
        /**
         * 是否启用验证码功能
         */
        private boolean enableVerifyCode = false;

        /**
         * 验证码过期时间
         */
        private int expireTimeSecond = 60; //过期时间：秒

    }

}
