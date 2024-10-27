package com.lightframework.comm.tcp.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * tcp server
 */
@Slf4j
public class TcpServer {

    private TcpServerConfig serverConfig;

    private TcpServerManager tcpServerManager;

    private EventLoopGroup bossGroup;//处理连接请求
    private EventLoopGroup workGroup;//处理收发数据

    private TcpServer(){}

    public static TcpServer start(TcpServerConfig tcpServerConfig) {
        TcpServer tcpServer = new TcpServer();
        tcpServer.serverConfig = tcpServerConfig;
        tcpServer.tcpServerManager = new TcpServerManager();
        tcpServer.start();
        return tcpServer;
    }

    private void start() {
        try {
            bossGroup = new NioEventLoopGroup(serverConfig.getBossThreadCount());
            workGroup = new NioEventLoopGroup(serverConfig.getWorkThreadCount());
            ServerBootstrap bootstrap = new ServerBootstrap();
            InetSocketAddress inetSocketAddress = serverConfig.getHost() != null && !serverConfig.getHost().isEmpty()
                    ?new InetSocketAddress(serverConfig.getHost(), serverConfig.getPort())
                    :new InetSocketAddress(serverConfig.getPort());
            bootstrap.group(bossGroup, workGroup)
                    .localAddress(inetSocketAddress)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, serverConfig.getBacklog())
                    .childOption(ChannelOption.SO_KEEPALIVE, serverConfig.isKeepalive())
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            serverConfig.getInitializationHandler().initChannel(socketChannel);
                            socketChannel.pipeline().addLast(new TcpServerHandler());
                            socketChannel.pipeline().addLast(new IdleCheckHandler(serverConfig.getReaderIdleTimeSeconds()));
                        }
                    });
            ChannelFuture future = bootstrap.bind().sync();
            if (future.isSuccess()) {
                log.info("{}服务启动成功！IP:{},PORT:{}",serverConfig.getName(),inetSocketAddress.getAddress().getHostAddress(),serverConfig.getPort());
            } else {
                throw new Exception(future.cause());
            }
        } catch (Exception e) {
            log.info(serverConfig.getName() +"服务启动失败！");
            throw new RuntimeException(serverConfig.getName() + "服务启动时发生异常",e);
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

    public TcpServerManager getTcpServerManager(){
        return tcpServerManager;
    }

    private class TcpServerHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            if(!((cause instanceof IOException) && cause.getMessage().equals("远程主机强迫关闭了一个现有的连接。"))) {
                log.error(serverConfig.getName() + "捕获异常：" + cause.getMessage(), cause);
            }
            ctx.channel().close();
        }
    }
}
