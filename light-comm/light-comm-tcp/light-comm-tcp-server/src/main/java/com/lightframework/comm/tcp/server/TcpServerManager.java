package com.lightframework.comm.tcp.server;

import com.lightframework.comm.tcp.common.handler.FailMessageHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*** 
 * @author yg
 * @date 2024/6/3 14:54
 * @version 1.0
 */
@Slf4j
public class TcpServerManager {

    private final ConcurrentHashMap<String, Channel> CHANNELS = new ConcurrentHashMap<>();

    private FailMessageHandler failMessageHandler;

    TcpServerManager(TcpServerConfig tcpServerConfig){
        this.failMessageHandler = new FailMessageHandler(tcpServerConfig.getName());
    }

    public void putChannel(String id,Channel channel){
        if(channel != null) {
            CHANNELS.put(id, channel);
        }
    }

    public void removeChannel(String id){
        CHANNELS.remove(id);
    }

    public void removeChannel(Channel channel){
        Iterator<Map.Entry<String, Channel>> iterator = CHANNELS.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, Channel> next = iterator.next();
            if(next.getValue() == channel){
                iterator.remove();
                break;
            }
        }
    }

    public Channel getChannel(String id){
        return CHANNELS.get(id);
    }

    public ChannelFuture sendMessage(Channel channel, Object message){
        if(channel == null){
            return null;
        }
        ChannelFuture channelFuture = channel.writeAndFlush(message);
        channelFuture.addListener(failMessageHandler);
        return channelFuture;
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

    public ConcurrentHashMap<String, Channel> channels(){
        return CHANNELS;
    }

}
