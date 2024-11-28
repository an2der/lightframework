package com.lightframework.comm.tcp.client;

import com.lightframework.comm.tcp.common.handler.FailMessageHandler;
import com.lightframework.common.LightException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

@Slf4j
public class TcpClient {

    private EventLoopGroup group;

    private Bootstrap bootstrap;

    private Channel channel;

    private TcpClientConfig clientConfig;

    private FailMessageHandler failMessageHandler;

    private volatile boolean disconnected = false;

    public TcpClient(TcpClientConfig tcpClientConfig){
        this.group = new NioEventLoopGroup(tcpClientConfig.getThreadCount());
        this.clientConfig = tcpClientConfig;
        this.failMessageHandler = new FailMessageHandler(tcpClientConfig.getName());
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

    public synchronized boolean connect(){
        if(channel == null || !channel.isActive()) {
            this.disconnected = false;
            try {
                ChannelFuture future = bootstrap.connect().sync();
                if (future.isSuccess()) {
                    channel = future.channel();
                    InetSocketAddress socketAddress = (InetSocketAddress) channel.localAddress();
                    log.info("{}连接服务端成功！LocalPort:{};Remote Server IP:{},PORT:{}",clientConfig.getName(),socketAddress.getPort(),clientConfig.getServerHost(), clientConfig.getServerPort());
                    return true;
                }
            } catch (Exception e) {

            }
            log.info("{}连接服务端失败！Remote Server IP:{},PORT:{}",clientConfig.getName(),clientConfig.getServerHost(), clientConfig.getServerPort());
            reconnect();
        }else {
            log.info(clientConfig.getName() + "客户端已连接，请勿重复连接！");
        }
        return false;
    }

    public Channel getChannel(){
        return this.channel;
    }

    public ChannelFuture sendMessage(Object message){
        if(this.channel == null){
            throw new LightException(clientConfig.getName()+"服务没有启动！");
        }
        ChannelFuture channelFuture = this.channel.writeAndFlush(message);
        channelFuture.addListener(failMessageHandler);
        return channelFuture;
    }


    public void disconnect(){
        if(this.channel != null) {
            this.disconnected = true;
            this.channel.close();//关闭TCP连接
            this.channel = null;
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

    private void reconnect(){
        if(!disconnected && (channel == null || !channel.isActive()) && clientConfig.getReconnectInterval() >0) {
            group.schedule(() ->{
                log.info("{}尝试重新连接到服务端！Remote Server IP:{},PORT:{}",clientConfig.getName(),clientConfig.getServerHost(), clientConfig.getServerPort());
                connect();
            }, clientConfig.getReconnectInterval(), TimeUnit.SECONDS);
        }
    }

    private class TcpClientHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            log.info(clientConfig.getName() + "连接断开！");
            reconnect();
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
