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

    @Autowired(required = false)
    private MqttWillMessage mqttWillMessage;

    @Bean(MqttClientManagerHolder.MQTT_CLIENT_MANAGER_NAME)
    public MqttClientManager buildMqttClientManager(){
        properties.setMqttDataReceiver(mqttDataReceiver);
        if(mqttWillMessage != null) {
            properties.setWill(mqttWillMessage.getTopic(), mqttWillMessage.getPayload(), mqttWillMessage.getQos(), mqttWillMessage.isRetained());
        }
        return new MqttClientManager(properties);
    }
}
