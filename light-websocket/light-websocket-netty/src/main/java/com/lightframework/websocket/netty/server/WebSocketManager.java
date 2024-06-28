package com.lightframework.websocket.netty.server;

import com.alibaba.fastjson2.JSON;
import com.lightframework.websocket.common.model.WebSocketMessage;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

/*** 
 * @author yg
 * @date 2024/6/3 14:54
 * @version 1.0
 */
@Slf4j
public class WebSocketManager {

    private static final ConcurrentHashMap<String, Channel> CHANNELS = new ConcurrentHashMap<>();

    private WebSocketManager(){}

    public static void putChannel(Channel channel){
        CHANNELS.put(channel.id().asLongText(), channel);
    }

    public static void removeChannel(Channel channel){
        CHANNELS.remove(channel.id().asLongText());
    }

    private static void sendMessage(Channel channel, String message){
        if(channel != null && channel.isOpen()) {
            channel.writeAndFlush(new TextWebSocketFrame(message));
        }
    }

    public static void sendMessage(Channel channel, WebSocketMessage message){
        sendMessage(channel,JSON.toJSONString(message));
    }

    /**
     * 给指定用户发送信息
     *
     * @param clientId
     * @param message
     */
    public static void sendMessage(String clientId, WebSocketMessage message) {
        Channel channel = CHANNELS.get(clientId);
        sendMessage(channel, message);
    }

    /**
     * 群发自定义消息
     */
    public static void sendMessageToAll(WebSocketMessage message){
        String stringMessage = JSON.toJSONString(message);
        CHANNELS.entrySet().forEach(e->sendMessage(e.getValue(),stringMessage));
    }

}
