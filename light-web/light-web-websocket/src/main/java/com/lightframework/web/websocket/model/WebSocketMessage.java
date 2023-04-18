package com.lightframework.web.websocket.model;

/** websocket消息对象
 * @author yg
 * @date 2022/10/10 15:51
 * @version 1.0
 */
public class WebSocketMessage {

    public static final int SESSION_ID_MESSAGE_TYPE = 0;

    private int type;

    private Object data;

    public WebSocketMessage(int type, Object data) {
        this.type = type;
        this.data = data;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
