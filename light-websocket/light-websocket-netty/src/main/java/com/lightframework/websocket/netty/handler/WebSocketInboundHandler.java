package com.lightframework.websocket.netty.handler;

import com.alibaba.fastjson2.JSON;
import com.lightframework.websocket.common.constant.WebSocketMsgTypeConstants;
import com.lightframework.websocket.common.model.TextWebSocketMessage;
import com.lightframework.websocket.common.model.WebSocketMessage;
import com.lightframework.websocket.netty.server.WebSocketManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class WebSocketInboundHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private AbstractWebSocketHandler abstractWebSocketHandler;

    public WebSocketInboundHandler(AbstractWebSocketHandler abstractWebSocketHandler) {
        this.abstractWebSocketHandler = abstractWebSocketHandler;
    }

    /**
     * 连接建立成功调用的方法
     */
    @Override
    public void channelActive(ChannelHandlerContext context) {
        WebSocketManager.CHANNELS.put(context.channel().id().asLongText(), context.channel());
        try {
            WebSocketManager.sendMessage(context.channel(), new WebSocketMessage(WebSocketMsgTypeConstants.SESSION_ID,context.channel().id().asLongText()));
            abstractWebSocketHandler.open(context.channel());
        }catch (Exception e){
            log.error("WebSocket Open发生异常 ", e);
        }

    }

    /**
     * * 收到客户端消息后调用的方法
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame textWebSocketFrame)  {
        try {
            abstractWebSocketHandler.receive(ctx.channel(), JSON.parseObject(textWebSocketFrame.text(), TextWebSocketMessage.class));
        }catch (Exception e){
            log.error("WebSocket Message发生异常 ", e);
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @Override
    public void channelInactive(ChannelHandlerContext context)  {
        try {
            WebSocketManager.CHANNELS.remove(context.channel().id().asLongText());
            abstractWebSocketHandler.close(context.channel());
        } catch (Exception e) {
            log.error("WebSocket Close发生异常 ", e);
        }
    }

    /**
     * 发生错误时调用
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        abstractWebSocketHandler.error(context.channel(),cause);
        log.error("WebSocket 出现异常：", cause);
    }

}
