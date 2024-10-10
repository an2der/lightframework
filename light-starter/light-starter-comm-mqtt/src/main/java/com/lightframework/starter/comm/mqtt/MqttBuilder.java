package com.lightframework.starter.comm.mqtt;

import com.lightframework.comm.mqtt.MqttClientManager;
import com.lightframework.comm.mqtt.MqttDataReceiver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "mqtt",name = "enabled",havingValue = "true",matchIfMissing = true)
public class MqttBuilder {

    @Autowired
    private MqttProperties properties;

    @Autowired(required = false)
    private MqttDataReceiver mqttDataReceiver;

    @Bean
    public MqttClientManager buildMqttClientManager(){
        properties.setMqttDataReceiver(mqttDataReceiver);
        return new MqttClientManager(properties);
    }
}
