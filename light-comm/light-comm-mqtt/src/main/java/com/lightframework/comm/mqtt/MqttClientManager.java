package com.lightframework.comm.mqtt;

import com.lightframework.util.id.ShortSnowflakeId;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MqttClientManager {

    private MqttConfig mqttConfig;

    private MqttClient mqttClient;

    private volatile boolean disconnected = false;

    public MqttClientManager(MqttConfig mqttConfig){
        this.mqttConfig = mqttConfig;
    }

    public boolean connect(){
        disconnected = false;
        return connect(false);
    }

    private synchronized boolean connect(boolean isReconnect){
        if(mqttClient == null || !mqttClient.isConnected()) {
            try {
                mqttClient = new MqttClient(mqttConfig.getHost(), mqttConfig.getName() + "[" + mqttConfig.getClientId() + "]", new MemoryPersistence());
            } catch (MqttException e) {
                throw new RuntimeException(e);
            }
            mqttClient.setTimeToWait(mqttConfig.getTimeToWait());//设置客户端发送超时时间，防止无限阻塞。
            mqttClient.setCallback(new MqttCallback());
            try {
                mqttClient.connect(mqttConfig);
                return true;
            } catch (MqttException e) {
                log.info(mqttConfig.getName() + "连接失败！" + e.getMessage());
                if(!isReconnect) {
                    reconnect();
                }
                return false;
            }
        }else {
            log.info(mqttConfig.getName() + "已连接，请勿重复连接！");
            return true;
        }
    }

    private void reconnect(){
        if(!disconnected && mqttConfig.getReconnectInterval() > 0){
            new Thread(){
                {start();}
                @Override
                public void run() {
                    while (!disconnected && !mqttClient.isConnected()){
                        try {
                            TimeUnit.SECONDS.sleep(mqttConfig.getReconnectInterval());
                            if(!disconnected) {
                                log.info(mqttConfig.getName() + "开始重连...");
                                if (connect(true)) {
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            log.error(mqttConfig.getName()+" 重连时发生异常！",e);
                        }
                    }
                }
            };
        }
    }

    public boolean isConnected(){
        if(mqttClient != null) {
            return mqttClient.isConnected();
        }
        return false;
    }

    public void disconnect(){
        try {
            disconnected = true;
            if(mqttClient != null) {
                mqttClient.disconnect();
                mqttClient.close();
            }
        } catch (MqttException e) {
            log.error(mqttConfig.getName()+"关闭连接时发生异常",e);
        }
    }

    /**
     * 向主题发布消息，保留最后一条消息，并且 最多发一次 消息可能会丢失
     * @param topic
     * @param payload
     * @return
     */
    public boolean publishRetainedAndQos0(String topic,byte[] payload){
        return publish(topic,payload,0,true);
    }

    /**
     * 向主题发布消息，保留最后一条消息，并且 至少发一次 保证消息能被消费者接收到，但消息可能会重复
     * @param topic
     * @param payload
     * @return
     */
    public boolean publishRetainedAndQos1(String topic,byte[] payload){
        return publish(topic,payload,1,true);
    }

    /**
     * 向主题发布消息，保留最后一条消息，并且 确保只有一次 保证消息能被消费者接收到，并且只收到一次
     * @param topic
     * @param payload
     * @return
     */
    public boolean publishRetainedAndQos2(String topic,byte[] payload){
        return publish(topic,payload,2,true);
    }

    /**
     * 向主题发布消息，不保留最后一条消息，并且 最多发一次 消息可能会丢失
     * @param topic
     * @param payload
     * @return
     */
    public boolean publishNotRetainedAndQos0(String topic,byte[] payload){
        return publish(topic,payload,0,false);
    }

    /**
     * 向主题发布消息，不保留最后一条消息，并且 至少发一次 保证消息能被消费者接收到，但消息可能会重复
     * @param topic
     * @param payload
     * @return
     */
    public boolean publishNotRetainedAndQos1(String topic,byte[] payload){
        return publish(topic,payload,1,false);
    }

    /**
     * 向主题发布消息，不保留最后一条消息，并且 确保只有一次 保证消息能被消费者接收到，并且只收到一次
     * @param topic
     * @param payload
     * @return
     */
    public boolean publishNotRetainedAndQos2(String topic,byte[] payload){
        return publish(topic,payload,2,false);
    }

    /**
     * 向主题发布消息
     * @param topic 主题
     * @param payload 数据
     * @param qos 服务质量 0：最多发一次 消息可能会丢失；1：至少发一次 保证消息能被消费者接收到，但消息可能会重复；
     *            2：确保只有一次 保证消息能被消费者接收到，并且只收到一次
     * @param retained 是否保留该主题最后一条消息
     * @return
     */
    public boolean publish(String topic, byte[] payload,int qos, boolean retained){
        MqttMessage mqttMessage = buildMqttMessage(payload,qos, retained);
        return publish(topic,mqttMessage);
    }

    public boolean publish(String topic, MqttMessage mqttMessage){
        try {
            mqttClient.publish(topic, mqttMessage);
            return true;
        } catch (MqttException e) {
            log.error(mqttConfig.getName() + "发布消息发生异常", e);
        }
        return false;
    }

    /**
     *
     * @param payload 数据
     * @param qos 服务质量 0：最多发一次 消息可能会丢失；1：至少发一次 保证消息能被消费者接收到，但消息可能会重复；
     *            2：确保只有一次 保证消息能被消费者接收到，并且只收到一次
     * @param retained 是否保留该主题最后一条消息
     * @return
     */
    public MqttMessage buildMqttMessage(byte[] payload,int qos, boolean retained) {
        MqttMessage mqttMsg = new MqttMessage();
        mqttMsg.setId(ShortSnowflakeId.getNextId());
        mqttMsg.setQos(qos);
        mqttMsg.setPayload(payload);
        mqttMsg.setRetained(retained);
        return mqttMsg;
    }

    private class MqttCallback implements MqttCallbackExtended {


        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            log.info(mqttConfig.getName() + "连接成功!");
            try {
                mqttClient.subscribe(mqttConfig.getTopicFilters());
                log.info(mqttConfig.getName() + "订阅主题成功！");
            } catch (MqttException e) {
                log.info(mqttConfig.getName() + "订阅主题失败！");
            }
        }

        @Override
        public void connectionLost(Throwable throwable) {
            log.info(mqttConfig.getName() + "断开连接!");
            reconnect();
        }

        @Override
        public void messageArrived(String topic, MqttMessage mqttMessage) {
            if(mqttConfig.getMqttDataReceiver() != null) {
                try {
                    mqttConfig.getMqttDataReceiver().receive(topic, mqttMessage.getPayload());
                } catch (Exception e) {
                    log.error(mqttConfig.getName() + "处理消息数据时发生异常", e);
                }
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            //发布消息回调
        }
    }

}
