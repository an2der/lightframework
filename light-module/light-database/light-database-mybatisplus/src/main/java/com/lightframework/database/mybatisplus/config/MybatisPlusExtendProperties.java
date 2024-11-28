package com.lightframework.database.mybatisplus.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@SuppressWarnings("all")
@ConfigurationProperties(prefix = "mybatis-plus")
@Getter
@Setter
public class MybatisPlusExtendProperties {
    /**
     * Packages to search mapper. (Package delimiters are ",; \t\n"
     */
    private String mapperScanPackage;
}
