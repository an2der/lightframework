package com.lightframework.starter.comm.mqtt;

import com.lightframework.comm.mqtt.MqttConfig;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "mqtt")
public class MqttProperties extends MqttConfig {

    /**
     * 是否开启mqtt
     */
    private boolean enabled = true;

    /**
     * 无效，改为reconnectInterval控制是否重新连接
     */
    private boolean automaticReconnect = false;

    /**
     * 遗嘱消息
     */
    private MqttWillMessage willMsg;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public MqttWillMessage getWillMsg() {
        return willMsg;
    }

    public void setWillMsg(MqttWillMessage willMsg) {
        this.willMsg = willMsg;
    }

    public static class MqttWillMessage {
        /**
         * 遗嘱消息主题
         */
        private String topic;
        /**
         * 遗嘱消息数据
         */
        private String payload;
        /**
         * 消息服务质量
         */
        private int qos = 2;
        /**
         * 是否保留最后一条消息
         */
        private boolean retained = false;

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public String getPayload() {
            return payload;
        }

        public void setPayload(String payload) {
            this.payload = payload;
        }

        public int getQos() {
            return qos;
        }

        public void setQos(int qos) {
            this.qos = qos;
        }

        public boolean isRetained() {
            return retained;
        }

        public void setRetained(boolean retained) {
            this.retained = retained;
        }
    }
}
