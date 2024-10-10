package com.lightframework.starter.comm.mqtt;

import com.lightframework.comm.mqtt.MqttClientManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "mqtt",name = "enabled",havingValue = "true",matchIfMissing = true)
public class MqttRunner implements CommandLineRunner {

    @Autowired
    private MqttClientManager mqttClientManager;

    @Override
    public void run(String... args) {
        mqttClientManager.connect();
    }

}
