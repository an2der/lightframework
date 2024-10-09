package com.lightframework.starter.comm.mqtt;

import com.lightframework.comm.mqtt.MqttClientManager;
import com.lightframework.comm.mqtt.MqttDataReceiver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
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
