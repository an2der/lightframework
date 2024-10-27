package com.lightframework.comm.tcp.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

/*** 
 * @author yg
 * @date 2024/6/3 14:54
 * @version 1.0
 */
@Slf4j
public class TcpServerManager {

    private final ConcurrentHashMap<String, Channel> CHANNELS = new ConcurrentHashMap<>();

    public void putChannel(String id,Channel channel){
        if(channel != null) {
            CHANNELS.put(id, channel);
        }
    }

    public void removeChannel(String id){
        CHANNELS.remove(id);
    }

    public Channel getChannel(String id){
        return CHANNELS.get(id);
    }

    public ChannelFuture sendMessage(Channel channel, Object message){
        if(channel == null){
            return null;
        }
        return channel.writeAndFlush(message);
    }

    /**
     * 给指定用户发送信息
     *
     * @param clientId
     * @param message
     */
    public ChannelFuture sendMessage(String clientId, Object message) {
        Channel channel = CHANNELS.get(clientId);
        return sendMessage(channel, message);
    }

    public ConcurrentHashMap<String, Channel> getAllChannel(){
        return CHANNELS;
    }

}
