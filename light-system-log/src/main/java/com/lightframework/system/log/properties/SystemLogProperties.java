package com.lightframework.system.log.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "system.log")
@Getter
@Setter
public class SystemLogProperties {

    private int reservedDays = 365;
}
