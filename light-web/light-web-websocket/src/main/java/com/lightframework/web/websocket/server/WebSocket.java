package com.lightframework.web.websocket.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lightframework.web.websocket.model.WebSocketMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/** websocket Server
 * @author yg
 * @date 2022/6/14 19:19
 * @version 1.0
 */
@ServerEndpoint("/websocket")
@Component
public class WebSocket {

    private static Logger log = LoggerFactory.getLogger(WebSocket.class);

    private static final ConcurrentHashMap<String, Session> SESSIONS = new ConcurrentHashMap<>();

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) throws JsonProcessingException {
        SESSIONS.put(session.getId(), session);
        sendMessage(session, new ObjectMapper().writeValueAsString(new WebSocketMessage(WebSocketMessage.SESSION_ID_MESSAGE_TYPE,session.getId())));
    }


    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session) {
        try {
            SESSIONS.remove(session.getId());
            session.close();
        } catch (IOException e) {
            log.error("WebSocket onClose ", e);
        }
    }

    /**
     * * 收到客户端消息后调用的方法 * * @param message * 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("用户发送过来的消息为：" + message);
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        onClose(session);
        log.warn("WebSocket出现错误", error);
    }

    public static void sendMessage(Session session,String message){
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            log.error("WebSocket sendMessage", e);
        }
    }

    /**
     * 给指定用户发送信息
     *
     * @param clientId
     * @param message
     */
    public static void sendMessageToSingleClient(String clientId, String message) {
        Session session = SESSIONS.get(clientId);
        try {
            sendMessage(session, message);
        } catch (Exception e) {
            log.error("WebSocket sendInfo", e);
        }
    }

    /**
     * 群发自定义消息
     */
    public static void sendMessageToAll(String message){
        try {
            SESSIONS.entrySet().forEach(e->sendMessage(e.getValue(),message));
        } catch (Exception e) {
            log.error("WebSocket sendInfo", e);
        }
    }
}
