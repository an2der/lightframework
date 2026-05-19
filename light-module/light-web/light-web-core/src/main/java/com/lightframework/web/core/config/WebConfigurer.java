package com.lightframework.web.core.config;

import com.lightframework.web.core.properties.WebProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebConfigurer implements WebMvcConfigurer {

    @Autowired
    private WebProperties webProperties;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        webProperties.getCors().getMappings().forEach((k, v) -> {
            CorsRegistration corsRegistration = registry.addMapping(k);
            if(v != null){
                if(v.getAllowedOrigins() != null){
                    corsRegistration.allowedOrigins(v.getAllowedOrigins().toArray(new String[0]));
                }
                if(v.getAllowedOriginPatterns() != null){
                    corsRegistration.allowedOriginPatterns(v.getAllowedOriginPatterns().toArray(new String[0]));
                }
                if(v.getAllowedMethods() != null){
                    corsRegistration.allowedMethods(v.getAllowedMethods().toArray(new String[0]));
                }
                if(v.getAllowedHeaders() != null){
                    corsRegistration.allowedHeaders(v.getAllowedHeaders().toArray(new String[0]));
                }
                if(v.getExposedHeaders() != null){
                    corsRegistration.exposedHeaders(v.getExposedHeaders().toArray(new String[0]));
                }
                if(v.getAllowCredentials() != null){
                    corsRegistration.allowCredentials(v.getAllowCredentials());
                }
                if(v.getMaxAge() != null){
                    corsRegistration.maxAge(v.getMaxAge());
                }
            }
        });
    }
}
