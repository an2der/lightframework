package com.lightframework.websocket.common.model;

public class TextWebSocketMessage extends WebSocketMessage<String>{

    public TextWebSocketMessage(){
    }

    public TextWebSocketMessage(int type, String payload) {
        super(type, payload);
    }

    public TextWebSocketMessage(long seq,int type, String payload) {
        super(seq,type, payload);
    }
}
