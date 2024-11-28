package com.lightframework.websocket.tomcat.server;

import com.lightframework.websocket.common.model.TextWebSocketMessage;

import javax.websocket.Session;

public abstract class AbstractWebSocketHandler {

    public abstract void receive(Session session, TextWebSocketMessage webSocketMessage);

    public void open(Session session){

    }

    public void close(Session session){

    }

    public void error(Session session, Throwable error){

    }

}
