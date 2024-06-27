package com.lightframework.websocket.tomcat.server;

import com.alibaba.fastjson2.JSON;
import com.lightframework.websocket.common.constant.WebSocketMsgTypeConstants;
import com.lightframework.websocket.common.model.TextWebSocketMessage;
import com.lightframework.websocket.common.model.WebSocketMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;


/** websocket Server
 * @author yg
 * @date 2022/6/14 19:19
 * @version 1.0
 */
@ServerEndpoint("/websocket")
@Component
@Slf4j
public class WebSocketServer {

    private static AbstractWebSocketHandler abstractWebSocketHandler;

    @Autowired
    public void setAbstractWebSocketHandler(AbstractWebSocketHandler abstractWebSocketHandler) {
        WebSocketServer.abstractWebSocketHandler = abstractWebSocketHandler;
    }

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) {
        WebSocketManager.SESSIONS.put(session.getId(), session);
        try {
            WebSocketManager.sendMessage(session, new WebSocketMessage(WebSocketMsgTypeConstants.SESSION_ID,session.getId()));
            abstractWebSocketHandler.open(session);
        }catch (Exception e){
            log.error("WebSocket Open发生异常 ", e);
        }
    }


    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session) {
        try {
            WebSocketManager.SESSIONS.remove(session.getId());
            abstractWebSocketHandler.close(session);
        } catch (Exception e) {
            log.error("WebSocket Close发生异常 ", e);
        }
    }

    /**
     * * 收到客户端消息后调用的方法 * * @param message * 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            abstractWebSocketHandler.receive(session,JSON.parseObject(message,TextWebSocketMessage.class));
        }catch (Exception e){
            log.error("WebSocket Message发生异常 ", e);
        }

    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        abstractWebSocketHandler.error(session,error);
        log.error("WebSocket 出现异常", error);
    }

}
