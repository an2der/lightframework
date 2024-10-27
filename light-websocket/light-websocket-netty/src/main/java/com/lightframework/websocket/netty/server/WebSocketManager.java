package com.lightframework.websocket.netty.server;

import com.alibaba.fastjson2.JSON;
import com.lightframework.comm.tcp.server.TcpServerManager;
import com.lightframework.websocket.common.model.WebSocketMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
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

    static TcpServerManager tcpServerManager;

    private WebSocketManager(){}

    public static void putChannel(Channel channel){
        if(channel != null) {
            tcpServerManager.putChannel(channel.id().asLongText(), channel);
        }
    }

    public static void removeChannel(Channel channel){
        tcpServerManager.removeChannel(channel.id().asLongText());
    }

    public static Channel getChannel(String channelId){
        return tcpServerManager.getChannel(channelId);
    }

    private static ChannelFuture sendMessage(Channel channel, String message){
        if(channel != null) {
            return channel.writeAndFlush(new TextWebSocketFrame(message));
        }
        return null;
    }

    public static ChannelFuture sendMessage(Channel channel, WebSocketMessage message){
        return sendMessage(channel,JSON.toJSONString(message));
    }

    /**
     * 给指定用户发送信息
     *
     * @param clientId
     * @param message
     */
    public static ChannelFuture sendMessage(String clientId, WebSocketMessage message) {
        Channel channel = tcpServerManager.getChannel(clientId);
        return sendMessage(channel, message);
    }

    /**
     * 群发自定义消息
     */
    public static void sendMessageToAll(WebSocketMessage message){
        String stringMessage = JSON.toJSONString(message);
        tcpServerManager.getAllChannel().entrySet().forEach(e->sendMessage(e.getValue(),stringMessage));
    }

    public static ConcurrentHashMap<String, Channel> getAllChannel(){
        return tcpServerManager.getAllChannel();
    }

}
