package com.lightframework.starter.comm.mqtt;

import com.lightframework.comm.mqtt.MqttClientManager;
import com.lightframework.comm.mqtt.MqttConfig;
import com.lightframework.comm.mqtt.MqttDataReceiver;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "mqtt",name = "enabled",havingValue = "true",matchIfMissing = true)
public class MqttStarter implements ApplicationRunner {

    @Autowired
    private MqttProperties properties;

    @Autowired(required = false)
    private MqttDataReceiver mqttDataReceiver;

    @Bean(MqttClientManagerHolder.MQTT_CLIENT_MANAGER_NAME)
    public MqttClientManager buildMqttClientManager(){
        MqttConfig mqttConfig = new MqttConfig();
        BeanUtils.copyProperties(properties,mqttConfig);
        if(properties.getWillMessage() != null) {
            mqttConfig.setWillMessage(new MqttConfig.MqttWillMessage());
            BeanUtils.copyProperties(properties.getWillMessage(), mqttConfig.getWillMessage());
        }
        mqttConfig.setMqttDataReceiver(mqttDataReceiver);
        return new MqttClientManager(mqttConfig);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        buildMqttClientManager().connect();
    }
}
