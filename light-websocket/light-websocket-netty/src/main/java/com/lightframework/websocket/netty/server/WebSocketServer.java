package com.lightframework.websocket.netty.server;


import com.lightframework.util.spring.SpringContextUtil;
import com.lightframework.websocket.netty.handler.AbstractWebSocketHandler;
import com.lightframework.websocket.netty.handler.IdleCheckHandler;
import com.lightframework.websocket.netty.handler.WebSocketInboundHandler;
import com.lightframework.websocket.netty.properties.WebSocketConfigProperties;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
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

    private EventLoopGroup bossGroup;
    private EventLoopGroup workGroup;

    @Override
    public void run(ApplicationArguments args) {
        bossGroup = new NioEventLoopGroup(webSocketConfigProperties.getBossThreadCount());
        workGroup = new NioEventLoopGroup(webSocketConfigProperties.getWorkThreadCount());
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workGroup)
        .localAddress(webSocketConfigProperties.getPort())
        .channel(NioServerSocketChannel.class)
        .option(ChannelOption.SO_BACKLOG, webSocketConfigProperties.getBacklog())
        .childOption(ChannelOption.SO_KEEPALIVE, webSocketConfigProperties.isKeepalive())
        .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast(new IdleCheckHandler(webSocketConfigProperties.getReaderIdleTimeSeconds()));
                // webSocket协议本身是基于http协议的，所以这边也要使用http编解码器
                socketChannel.pipeline().addLast(new HttpServerCodec());
                // 以块的方式来写的处理器
                socketChannel.pipeline().addLast(new ChunkedWriteHandler());
                // 聚合器，将HttpMessage和HttpContent集合成FullHttpRequest，设置单次请求文件的大小
                socketChannel.pipeline().addLast(new HttpObjectAggregator(1024 * 64));
                // websocket服务器处理的协议，用于指定给客户端连接访问的路由
                socketChannel.pipeline().addLast(new WebSocketServerProtocolHandler(webSocketConfigProperties.getWebsocketPath()));
                // 业务事件处理
                socketChannel.pipeline().addLast(new WebSocketInboundHandler(abstractWebSocketHandler));
            }
        });
        try {
            ChannelFuture future = bootstrap.bind().sync();
            if (future.isSuccess()) {
                log.info("WebSocket服务启动成功！");
            } else {
                throw new Exception(future.cause());
            }
        } catch (Exception e) {
            log.info("WebSocket服务启动失败！");
            log.error("WebSocket服务启动时发生异常", e);
            SpringContextUtil.exit();
        }

    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        try {
            if (bossGroup != null) {
                bossGroup.shutdownGracefully().sync();
            }
            if (workGroup != null) {
                workGroup.shutdownGracefully().sync();
            }
        } catch (InterruptedException e) {

        }
    }
}
