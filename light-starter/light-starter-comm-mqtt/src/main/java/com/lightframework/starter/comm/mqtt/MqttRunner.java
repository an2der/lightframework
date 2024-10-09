package com.lightframework.starter.comm.mqtt;

import com.lightframework.comm.mqtt.MqttClientManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MqttRunner implements CommandLineRunner {

    @Autowired
    private MqttClientManager mqttClientManager;

    @Override
    public void run(String... args) {
        mqttClientManager.connect();
    }

}
