package com.lightframework.websocket.tomcat.server;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
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
        WebSocketManager.sendMessage(session, new WebSocketMessage(WebSocketMsgTypeConstants.SESSION_ID,session.getId()));
        abstractWebSocketHandler.open(session);
    }


    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session) {
        try {
            WebSocketManager.SESSIONS.remove(session.getId());
            session.close();
            abstractWebSocketHandler.close(session);
        } catch (IOException e) {
            log.error("WebSocket Close发生异常 ", e);
        }
    }

    /**
     * * 收到客户端消息后调用的方法 * * @param message * 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            JSONObject jsonObject = JSONUtil.parseObj(message);
            if(jsonObject.containsKey("type")){
                abstractWebSocketHandler.receive(session,jsonObject.toBean(TextWebSocketMessage.class));
            }else {
                log.warn("无效的WebSocket消息：{}",message);
            }
        }catch (Exception e){
            log.error("WebSocket Message发生异常 ", e);
        }

    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        onClose(session);
        abstractWebSocketHandler.error(session,error);
        log.error("WebSocket出现错误", error);
    }

}
