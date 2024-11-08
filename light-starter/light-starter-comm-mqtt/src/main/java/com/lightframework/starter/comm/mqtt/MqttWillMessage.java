package com.lightframework.starter.comm.mqtt;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MqttWillMessage {
    private String topic;
    private byte[] payload;
    private int qos;
    private boolean retained;

}
