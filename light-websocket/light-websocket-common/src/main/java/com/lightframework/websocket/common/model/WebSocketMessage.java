package com.lightframework.websocket.common.model;

/** websocket消息对象
 * @author yg
 * @date 2022/10/10 15:51
 * @version 1.0
 */
public class WebSocketMessage<T> {

    private int type;

    private T payload;

    public WebSocketMessage(int type, T payload) {
        this.type = type;
        this.payload = payload;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }
}
