package com.lightframework.starter.comm.mqtt;

import com.lightframework.comm.mqtt.MqttConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "mqtt")
public class MqttProperties extends MqttConfig {

    /**
     * 是否开启mqtt
     */
    private boolean enabled = true;

    /**
     * 无效，改为reconnectInterval控制是否重新连接
     */
    private boolean automaticReconnect = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
