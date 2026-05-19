package com.lightframework.web.core.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "web")
@Getter
@Setter
public class WebProperties {

    /**
     * 跨域配置
     */
    private CorsMappingProperties cors = new CorsMappingProperties();

    /**
     * CORS 跨域配置映射
     */
    @Getter
    @Setter
    public static class CorsMappingProperties {

        /**
         * 跨域配置映射 key: URL路径，value: 跨域配置
         */
        private Map<String, CorsConfiguration> mappings = new HashMap<>();

    }

}
