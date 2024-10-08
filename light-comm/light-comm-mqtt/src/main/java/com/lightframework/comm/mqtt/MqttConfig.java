package com.lightframework.comm.mqtt;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import java.util.UUID;

@Getter
@Setter
public class MqttConfig extends MqttConnectOptions {

    private String host;//mqtt服务器主机地址

    private String name = "MQTT CLIENT";//客户端名称

    private String clientId = UUID.randomUUID().toString();

    private int timeToWait = 10000;//发布消息超时时间 单位毫秒

    private String [] topicFilters;//订阅主题列表，推荐使用主题通配符 /+ 或者 /#，（例：'upgrade/' 订阅主题 'upgrade/clientId,用来接收中心发出的指令）

    private int reconnectInterval = 3;//重连时间间隔

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private boolean autoReconnect = false;

    private MqttDataReceiver mqttDataReceiver;

    public MqttConfig(){
        super.setMaxInflight(100);//最大并发条数
        super.setAutomaticReconnect(true); //断开自动重连
        super.setHttpsHostnameVerificationEnabled(false);//https证书验证
    }

    public void setPassword(String password) {
        super.setPassword(password.toCharArray());
    }

    @Override
    public void setAutomaticReconnect(boolean automaticReconnect) {
        this.autoReconnect = automaticReconnect;
    }

    @Override
    public boolean isAutomaticReconnect() {
        return this.autoReconnect;
    }
}
