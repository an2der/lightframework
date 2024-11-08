package com.lightframework.starter.comm.mqtt;

import com.lightframework.comm.mqtt.MqttClientManager;
import com.lightframework.util.spring.SpringBeanUtil;

public class MqttClientManagerHolder {

    private MqttClientManagerHolder(){}
    static final String MQTT_CLIENT_MANAGER_NAME = "mqttClientManager";
    private static MqttClientManager mqttClientManager = null;

    public static MqttClientManager getMqttClientManager(){
        if(mqttClientManager == null){
            mqttClientManager = (MqttClientManager) SpringBeanUtil.getBean(MQTT_CLIENT_MANAGER_NAME);
        }
        return mqttClientManager;
    }
}
