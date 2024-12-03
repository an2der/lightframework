package com.lightframework.starter.comm.mqtt;

import com.lightframework.util.id.ShortSnowflakeId;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "mqtt")
@Getter
@Setter
public class MqttProperties {

    /**
     * 是否开启mqtt
     */
    private boolean enabled = true;

    /**
     * mqtt服务器主机地址
     */
    private String serverUri;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 客户端名称
     */
    private String name = "MqttClient";

    /**
     * 客户端ID编号
     */
    private String clientId = String.valueOf(ShortSnowflakeId.getNextId());

    /**
     * 发布消息超时时间 单位毫秒
     */
    private int timeToWait = 10000;

    /**
     * 订阅主题列表，推荐使用主题通配符 /+ 或者 /#，（例：'upgrade/' 订阅主题 'upgrade/clientId,用来接收中心发出的指令）
     */
    private String [] topicFilters;

    /**
     * 重连时间间隔 (秒)，大于0自动重连
     */
    private int reconnectInterval = 3;
    /**
     * 心跳时间间隔 单位：秒
     */
    private int keepAliveInterval = 60;
    /**
     * 最大并发条数
     */
    private int maxInflight = 100;
    /**
     * 连接超时时间 单位：秒
     */
    private int connectionTimeout = 30;

    /**
     * 遗嘱消息
     */
    private MqttWillMessage willMessage;

    @Getter
    @Setter
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
    }
}
