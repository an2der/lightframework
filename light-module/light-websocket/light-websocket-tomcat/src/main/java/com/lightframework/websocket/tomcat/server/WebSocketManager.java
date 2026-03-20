package com.lightframework.websocket.tomcat.server;

import com.lightframework.util.spring.SpringJacksonUtil;
import com.lightframework.websocket.common.model.WebSocketMessage;
import lombok.extern.slf4j.Slf4j;

import javax.websocket.Session;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*** 
 * @author yg
 * @date 2024/6/3 14:54
 * @version 1.0
 */
@Slf4j
public class WebSocketManager {

    private static final ConcurrentHashMap<String, Session> SESSIONS = new ConcurrentHashMap<>();

    static SpringJacksonUtil springJacksonUtil;

    private WebSocketManager(){}

    public static void putSession(String id,Session session){
        if(session != null) {
            SESSIONS.put(id, session);
        }
    }

    public static void removeSession(String id){
        Session session = SESSIONS.get(id);
        if(session != null) {
            try {
                session.close();
            } catch (IOException e) {
            }
        }
        SESSIONS.remove(id);
    }

    public static void removeSession(Session session){
        Iterator<Map.Entry<String, Session>> iterator = SESSIONS.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, Session> next = iterator.next();
            if(next.getValue() == session){
                try {
                    session.close();
                } catch (IOException e) {
                }
                iterator.remove();
                break;
            }
        }
    }

    public static Session getSession(String sessionId){
        return SESSIONS.get(sessionId);
    }

    private static void sendMessage(Session session, String message){
        try {
            if(session != null && session.isOpen()) {
                synchronized (session) {
                    session.getBasicRemote().sendText(message);
                }
            }
        } catch (IOException e) {
            log.error("WebSocket sendMessage error!", e);
        }
    }

    public static void sendMessage(Session session, WebSocketMessage message){
        sendMessage(session, springJacksonUtil.serialize(message));
    }

    /**
     * 给指定用户发送信息
     *
     * @param clientId
     * @param message
     */
    public static void sendMessage(String clientId, WebSocketMessage message) {
        Session session = SESSIONS.get(clientId);
        sendMessage(session, message);
    }

    /**
     * 群发消息
     * @param sessions
     * @param message
     */
    public static void sendMessageToSessions(List<Session> sessions, WebSocketMessage message){
        String stringMessage = springJacksonUtil.serialize(message);
        sessions.forEach(session->sendMessage(session,stringMessage));
    }

    /**
     * 群发消息
     * @param sessionIds
     * @param message
     */
    public static void sendMessageToSessionIds(List<String> sessionIds, WebSocketMessage message){
        String stringMessage = springJacksonUtil.serialize(message);
        sessionIds.forEach(sessionId->sendMessage(SESSIONS.get(sessionId),stringMessage));
    }

    /**
     * 群发自定义消息
     */
    public static void sendMessageToAll(WebSocketMessage message){
        try {
            String stringMessage = springJacksonUtil.serialize(message);
            SESSIONS.entrySet().forEach(e->sendMessage(e.getValue(),stringMessage));
        } catch (Exception e) {
            log.error("WebSocket sendMessageToAll error!", e);
        }
    }

}
