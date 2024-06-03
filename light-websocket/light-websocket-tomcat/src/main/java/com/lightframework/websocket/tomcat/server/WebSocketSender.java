package com.lightframework.websocket.tomcat.server;

import cn.hutool.json.JSONUtil;
import com.lightframework.websocket.common.model.WebSocketMessage;
import com.lightframework.websocket.tomcat.cache.WebSocketCache;
import lombok.extern.slf4j.Slf4j;

import javax.websocket.Session;
import java.io.IOException;

/*** 
 * @author yg
 * @date 2024/6/3 14:54
 * @version 1.0
 */
@Slf4j
public class WebSocketSender {

    public static void sendMessage(Session session, WebSocketMessage message){
        try {
            if(session != null) {
                session.getBasicRemote().sendText(JSONUtil.toJsonStr(message));
            }
        } catch (IOException e) {
            log.error("WebSocket sendMessage error!", e);
        }
    }

    /**
     * 给指定用户发送信息
     *
     * @param clientId
     * @param message
     */
    public static void sendMessageToSingleClient(String clientId, WebSocketMessage message) {
        Session session = WebSocketCache.SESSIONS.get(clientId);
        try {
            sendMessage(session, message);
        } catch (Exception e) {
            log.error("WebSocket sendMessageToSingleClient error!", e);
        }
    }

    /**
     * 群发自定义消息
     */
    public static void sendMessageToAll(WebSocketMessage message){
        try {
            WebSocketCache.SESSIONS.entrySet().forEach(e->sendMessage(e.getValue(),message));
        } catch (Exception e) {
            log.error("WebSocket sendMessageToAll error!", e);
        }
    }

}
