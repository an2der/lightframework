package com.lightframework.websocket.tomcat.server;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lightframework.websocket.common.model.WebSocketMessage;
import lombok.extern.slf4j.Slf4j;

import javax.websocket.Session;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/*** 
 * @author yg
 * @date 2024/6/3 14:54
 * @version 1.0
 */
@Slf4j
public class WebSocketManager {

    public static final ConcurrentHashMap<String, Session> SESSIONS = new ConcurrentHashMap<>();

    public static void sendMessage(Session session, WebSocketMessage message){
        try {
            if(session != null && session.isOpen()) {
                synchronized (session) {
                    session.getBasicRemote().sendText(JSON.toJSONString(message));
                }
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
        Session session = SESSIONS.get(clientId);
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
            SESSIONS.entrySet().forEach(e->sendMessage(e.getValue(),message));
        } catch (Exception e) {
            log.error("WebSocket sendMessageToAll error!", e);
        }
    }

}
