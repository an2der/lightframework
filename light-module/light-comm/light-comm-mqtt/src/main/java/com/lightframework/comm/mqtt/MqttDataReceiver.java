package com.lightframework.comm.mqtt;

import org.eclipse.paho.client.mqttv3.MqttMessage;

/** mqtt数据接收
 * @Author yg
 * @Date 2024/10/8 9:59
 */
public interface MqttDataReceiver {
    void receive(String topic, MqttMessage message);
}
