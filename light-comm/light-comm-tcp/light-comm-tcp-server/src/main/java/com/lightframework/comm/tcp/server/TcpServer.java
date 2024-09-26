package com.lightframework.comm.tcp.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * tcp server
 */
@Slf4j
public class TcpServer {

    private TcpServerConfig tcpServerConfig;
    private ChannelInitializationHandler initializationHandler;

    private EventLoopGroup bossGroup;//处理连接请求
    private EventLoopGroup workGroup;//处理收发数据

    private TcpServer(){}

    public static TcpServer start(TcpServerConfig tcpServerConfig) {
        TcpServer tcpServer = new TcpServer();
        tcpServer.tcpServerConfig = tcpServerConfig;
        tcpServer.start();
        return tcpServer;
    }

    private void start() {
        bossGroup = new NioEventLoopGroup(tcpServerConfig.getBossThreadCount());
        workGroup = new NioEventLoopGroup(tcpServerConfig.getWorkThreadCount());
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, tcpServerConfig.getBacklog())
                .childOption(ChannelOption.SO_KEEPALIVE, tcpServerConfig.isKeepalive())
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new IdleCheckHandler(tcpServerConfig.getReaderIdleTimeSeconds()));
                        tcpServerConfig.getInitializationHandler().initChannel(socketChannel);
                    }
                });
        try {
            ChannelFuture future = tcpServerConfig.getHost() != null && !tcpServerConfig.getHost().isEmpty()
                    ?bootstrap.bind(tcpServerConfig.getHost(),tcpServerConfig.getPort()).sync()
                    :bootstrap.bind(tcpServerConfig.getPort()).sync();
            if (future.isSuccess()) {
                log.info(tcpServerConfig.getName() + "服务启动成功！");
            } else {
                throw new Exception(future.cause());
            }
        } catch (Exception e) {
            log.info(tcpServerConfig.getName() +"服务启动失败！");
            throw new RuntimeException(tcpServerConfig.getName() + "服务启动时发生异常",e);
        }
    }

    public void shutdown(){
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
