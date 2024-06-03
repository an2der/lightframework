package com.lightframework.websocket.tomcat.server;

import com.lightframework.websocket.common.model.TextWebSocketMessage;

import javax.websocket.Session;

public interface WebSocketReceiver {

    void receive(Session session, TextWebSocketMessage webSocketMessage);
}
