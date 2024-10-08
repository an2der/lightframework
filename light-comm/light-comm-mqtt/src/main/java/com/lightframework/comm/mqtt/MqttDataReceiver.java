package com.lightframework.comm.mqtt;

/** mqtt数据接收
 * @Author yg
 * @Date 2024/10/8 9:59
 */
public interface MqttDataReceiver {
    void receive(String topic,byte [] data);
}
