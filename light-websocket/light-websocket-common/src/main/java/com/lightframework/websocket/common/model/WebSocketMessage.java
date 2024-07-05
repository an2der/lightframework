package com.lightframework.websocket.common.model;

/** websocket消息对象
 * @author yg
 * @date 2022/10/10 15:51
 * @version 1.0
 */
public class WebSocketMessage<T> {

    private long seq;//序列号，请求和响应需要一致

    private int type;

    private T payload;

    public WebSocketMessage(long seq,int type, T payload) {
        this.seq = seq;
        this.type = type;
        this.payload = payload;
    }

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

    public long getSeq() {
        return seq;
    }

    public void setSeq(long seq) {
        this.seq = seq;
    }
}
