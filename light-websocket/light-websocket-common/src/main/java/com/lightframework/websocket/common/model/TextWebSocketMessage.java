package com.lightframework.websocket.common.model;

public class TextWebSocketMessage extends WebSocketMessage<String>{
    public TextWebSocketMessage(int type, String payload) {
        super(type, payload);
    }
}
