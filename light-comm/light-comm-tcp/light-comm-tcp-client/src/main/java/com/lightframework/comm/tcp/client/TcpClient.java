package com.lightframework.comm.tcp.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class TcpClient {

    private EventLoopGroup group;

    private Bootstrap bootstrap;

    private Channel channel;

    private TcpClientConfig clientConfig;

    private volatile boolean disconnected = false;

    public TcpClient(TcpClientConfig tcpClientConfig){
        this.group = new NioEventLoopGroup(tcpClientConfig.getThreadCount());
        this.clientConfig = tcpClientConfig;
        this.bootstrap = new Bootstrap();
        this.bootstrap.group(group)
            .channel(NioSocketChannel.class)
            .remoteAddress(clientConfig.getServerHost(), clientConfig.getServerPort())
            .option(ChannelOption.SO_KEEPALIVE, clientConfig.isKeepalive())
            .option(ChannelOption.TCP_NODELAY, clientConfig.isNoDelay())
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    clientConfig.getInitializationHandler().initChannel(socketChannel);
                    socketChannel.pipeline().addLast(new TcpClientHandler());
                    HeartBeatHandler.handle(socketChannel,clientConfig);
                }
            });
    }

    public synchronized boolean connection(){
        if(channel == null || !channel.isActive()) {
            this.disconnected = false;
            try {
                ChannelFuture future = bootstrap.connect().sync();
                if (future.isSuccess()) {
                    channel = future.channel();
                    log.info(clientConfig.getName() + "连接服务端成功！");
                    return true;
                }
            } catch (Exception e) {

            }
            log.info(clientConfig.getName() + "连接服务端失败！");
            reconnection();
        }else {
            log.info(clientConfig.getName() + "客户端已连接，请勿重复连接！");
        }
        return false;
    }

    public Channel getChannel(){
        return this.channel;
    }

    public void disconnection(){
        if(this.channel != null) {
            this.disconnected = true;
            this.channel.close();//关闭TCP连接
        }
    }

    public void destroy(){
        try {
            if (group != null) {
                group.shutdownGracefully().sync();
            }
        } catch (InterruptedException e) {
        }
    }

    private void reconnection(){
        if(!disconnected && (channel == null || !channel.isActive()) && clientConfig.getReconnectionInterval() >0) {
            group.schedule(() ->{
                log.info(clientConfig.getName() + "尝试重新连接到服务端！");
                connection();
            }, clientConfig.getReconnectionInterval(), TimeUnit.SECONDS);
        }
    }

    private class TcpClientHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            log.info(clientConfig.getName() + "连接断开！");
            reconnection();
            super.channelInactive(ctx);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            if(!((cause instanceof IOException) && cause.getMessage().equals("远程主机强迫关闭了一个现有的连接。"))) {
                log.error(clientConfig.getName() + "捕获异常：" + cause.getMessage(), cause);
            }
            ctx.channel().close();
        }
    }
}
