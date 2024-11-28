package com.lightframework.websocket.netty.handler;

import com.lightframework.websocket.common.model.TextWebSocketMessage;
import io.netty.channel.Channel;

public abstract class AbstractWebSocketHandler {

    public abstract void receive(Channel channel, TextWebSocketMessage webSocketMessage);

    public void open(Channel channel){

    }

    public void close(Channel channel){

    }

    public void error(Channel channel, Throwable error){

    }

}
