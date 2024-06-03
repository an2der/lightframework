package com.lightframework.websocket.tomcat.server;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.lightframework.websocket.common.constant.WebSocketMsgTypeConstants;
import com.lightframework.websocket.common.model.TextWebSocketMessage;
import com.lightframework.websocket.common.model.WebSocketMessage;
import com.lightframework.websocket.tomcat.cache.WebSocketCache;
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

    @Autowired
    private WebSocketReceiver webSocketReceiver;

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) {
        WebSocketCache.SESSIONS.put(session.getId(), session);
        WebSocketSender.sendMessage(session, new WebSocketMessage(WebSocketMsgTypeConstants.SESSION_ID,session.getId()));
    }


    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session) {
        try {
            WebSocketCache.SESSIONS.remove(session.getId());
            session.close();
        } catch (IOException e) {
            log.error("WebSocket Close发生异常 ", e);
        }
    }

    /**
     * * 收到客户端消息后调用的方法 * * @param message * 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        JSONObject jsonObject = JSONUtil.parseObj(message);
        if(jsonObject.containsKey("type")){
            webSocketReceiver.receive(session,jsonObject.toBean(TextWebSocketMessage.class));
        }else {
            log.warn("无效的WebSocket消息：{}",message);
        }
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        onClose(session);
        log.error("WebSocket出现错误", error);
    }

}
