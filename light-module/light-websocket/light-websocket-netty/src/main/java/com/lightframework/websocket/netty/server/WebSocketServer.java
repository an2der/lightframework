package com.lightframework.websocket.netty.server;


import com.lightframework.comm.tcp.server.TcpServer;
import com.lightframework.comm.tcp.server.TcpServerConfig;
import com.lightframework.util.spring.SpringContextUtil;
import com.lightframework.util.spring.web.SpringJacksonUtil;
import com.lightframework.websocket.netty.handler.AbstractWebSocketHandler;
import com.lightframework.websocket.netty.handler.WebSocketInboundHandler;
import com.lightframework.websocket.netty.properties.WebSocketConfigProperties;
import com.lightframework.websocket.netty.util.SSLUtil;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

/***
 * @author yg
 * @date 2024/6/27 16:45
 * @version 1.0
 */
@Component
@Slf4j
public class WebSocketServer implements ApplicationRunner, ApplicationListener<ContextClosedEvent> {

    @Autowired
    private AbstractWebSocketHandler abstractWebSocketHandler;

    @Autowired
    private WebSocketConfigProperties webSocketConfigProperties;

    @Autowired
    private ServerProperties serverProperties;

    @Autowired
    private SpringJacksonUtil springJacksonUtil;

    private TcpServer tcpServer;


    @Override
    public void run(ApplicationArguments args) {
        TcpServerConfig tcpServerConfig = new TcpServerConfig();
        BeanUtils.copyProperties(webSocketConfigProperties,tcpServerConfig);
        tcpServerConfig.setName("WebSocket");
        tcpServerConfig.setInitializationHandler(socketChannel -> {
            if(serverProperties.getSsl() != null && serverProperties.getSsl().isEnabled()){
                socketChannel.pipeline().addLast(new SslHandler(SSLUtil.createSSLEngine(serverProperties.getSsl().getKeyStore(),serverProperties.getSsl().getKeyPassword())));
            }
            // webSocket协议本身是基于http协议的，所以这边也要使用http编解码器
            socketChannel.pipeline().addLast(new HttpServerCodec());
            // 以块的方式来写的处理器
            socketChannel.pipeline().addLast(new ChunkedWriteHandler());
            // 聚合器，将HttpMessage和HttpContent集合成FullHttpRequest，设置单次请求文件的大小
            socketChannel.pipeline().addLast(new HttpObjectAggregator(webSocketConfigProperties.getMaxContentLength()));
            // websocket服务器处理的协议，用于指定给客户端连接访问的路由
            socketChannel.pipeline().addLast(new WebSocketServerProtocolHandler(webSocketConfigProperties.getWebsocketPath()));
            // 业务事件处理
            socketChannel.pipeline().addLast(new WebSocketInboundHandler(abstractWebSocketHandler,springJacksonUtil));
        });
        try {
            log.info("{}服务请求地址：{}",tcpServerConfig.getName(),webSocketConfigProperties.getWebsocketPath());
            tcpServer = TcpServer.start(tcpServerConfig);
            WebSocketManager.tcpServerManager = tcpServer.getTcpServerManager();
            WebSocketManager.springJacksonUtil = springJacksonUtil;
        }catch (Exception e){
            log.error(e.getMessage(),e.getCause());
            SpringContextUtil.exit();
        }
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        if(tcpServer != null) {
            tcpServer.shutdown();
        }
    }
}
