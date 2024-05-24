package com.lightframework.auth.core.properties;

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
@Getter
@Setter
public class AuthConfigProperties {

    private String tokenKey = "Authorization";

    private String secret = "LIGHT123456789ABCDEF";

    private int expireTimeMinute = 43200; //分钟

    private List<String> permitUrls;

    private VerifyCode verifyCode = new VerifyCode();


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
