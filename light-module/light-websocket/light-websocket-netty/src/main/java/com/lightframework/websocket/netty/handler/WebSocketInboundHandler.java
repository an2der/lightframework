package com.lightframework.websocket.netty.handler;

import com.lightframework.util.spring.SpringJacksonUtil;
import com.lightframework.websocket.common.constant.WebSocketConstants;
import com.lightframework.websocket.common.model.TextWebSocketMessage;
import com.lightframework.websocket.common.model.WebSocketMessage;
import com.lightframework.websocket.netty.server.WebSocketManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;


@Slf4j
public class WebSocketInboundHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private AbstractWebSocketHandler abstractWebSocketHandler;

    private SpringJacksonUtil springJacksonUtil;

    public WebSocketInboundHandler(AbstractWebSocketHandler abstractWebSocketHandler,SpringJacksonUtil springJacksonUtil) {
        this.abstractWebSocketHandler = abstractWebSocketHandler;
        this.springJacksonUtil = springJacksonUtil;
    }


    /**
     * * 收到客户端消息后调用的方法
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame textWebSocketFrame)  {
        try {
            abstractWebSocketHandler.receive(ctx.channel(), springJacksonUtil.deserialize(textWebSocketFrame.text(), TextWebSocketMessage.class));
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
            WebSocketManager.removeChannel(context.channel());
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
        //忽略SSL证书不受信错误
        if(cause.getCause() != null && cause.getCause() instanceof javax.net.ssl.SSLHandshakeException
                && cause.getCause().getMessage().contains("certificate_unknown")){
            return;
        }
        abstractWebSocketHandler.error(context.channel(),cause);
        log.error("WebSocket 出现异常：", cause);
    }

    /**
     * 连接建立成功调用的方法
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext context, Object obj) {
        if (obj instanceof WebSocketServerProtocolHandler.HandshakeComplete) {//netty WebSocket握手成功事件，这时候才算真正的连接成功
            WebSocketServerProtocolHandler.HandshakeComplete handshakeComplete = (WebSocketServerProtocolHandler.HandshakeComplete) obj;
            String requestUri = handshakeComplete.requestUri();

            // 解析 URL 参数
            QueryStringDecoder decoder = new QueryStringDecoder(requestUri);
            Map<String, List<String>> params = decoder.parameters();
            List<String> list = params.get(WebSocketConstants.PARAM_USER_ID);
            String userId = list != null && !list.isEmpty() ? list.get(0) : context.channel().id().asLongText();
            WebSocketManager.putChannel(userId, context.channel());
            try {
                WebSocketManager.sendMessage(context.channel(), new WebSocketMessage(WebSocketConstants.SESSION_ID,userId));
                abstractWebSocketHandler.open(context.channel());
            }catch (Exception e){
                log.error("WebSocket Open发生异常 ", e);
            }
        }
    }

}
