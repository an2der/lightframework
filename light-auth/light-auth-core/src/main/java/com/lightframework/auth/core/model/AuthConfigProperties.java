package com.lightframework.auth.core.model;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/** 授权配置对象
 * @author yg
 * @date 2023/5/31 14:38
 * @version 1.0
 */
@ConfigurationProperties(prefix = "auth")
public class AuthConfigProperties {

    private InterceptUrl interceptUrl;

    private Integer expireTimeMinute; //分钟

    public Integer getExpireTimeMinute() {
        return expireTimeMinute;
    }

    public void setExpireTimeMinute(Integer expireTimeMinute) {
        this.expireTimeMinute = expireTimeMinute;
    }

    public InterceptUrl getInterceptUrl() {
        return interceptUrl;
    }

    public void setInterceptUrl(InterceptUrl interceptUrl) {
        this.interceptUrl = interceptUrl;
    }

    public class InterceptUrl{
        private List<String> includes;

        private List<String> excludes;

        public List<String> getIncludes() {
            return includes;
        }

        public void setIncludes(List<String> includes) {
            this.includes = includes;
        }

        public List<String> getExcludes() {
            return excludes;
        }

        public void setExcludes(List<String> excludes) {
            this.excludes = excludes;
        }
    }
}
