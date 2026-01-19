package com.lightframework.comm.tcp.client;

import com.lightframework.comm.tcp.common.handler.FailMessageHandler;
import com.lightframework.comm.tcp.common.handler.IdleCheckHandler;
import com.lightframework.comm.tcp.common.heartbeat.HeartBeatHandler;
import com.lightframework.common.LightException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import static com.lightframework.comm.tcp.client.TcpMultiClientManager.CLIENT_ID;

@Slf4j
public class TcpClient {

    private String id;

    private EventLoopGroup group;

    private boolean shareGroup;

    private Bootstrap bootstrap;

    private Channel channel;

    private TcpClientConfig clientConfig;

    private FailMessageHandler failMessageHandler;

    private volatile boolean disconnected = false;

    public TcpClient(TcpClientConfig tcpClientConfig){
        this(tcpClientConfig, new NioEventLoopGroup(tcpClientConfig.getThreadCount()),false);
    }

    TcpClient(TcpClientConfig tcpClientConfig,EventLoopGroup eventLoopGroup,boolean shareGroup){
        this.shareGroup = shareGroup;
        this.group = eventLoopGroup;
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
                        socketChannel.pipeline().addFirst(new TcpClientHandler());
                        HeartBeatHandler.handle(socketChannel,clientConfig.getName(),group,clientConfig.getHeartBeatConfig());
                        if(clientConfig.getReaderIdleTimeSeconds() > 0) {
                            socketChannel.pipeline().addFirst(new IdleCheckHandler(clientConfig.getReaderIdleTimeSeconds()));
                        }
                        socketChannel.pipeline().addLast(new TcpClientExceptionHandler());
                    }
                });
    }

    public synchronized boolean connect(){
        this.disconnected = false;
        return connectToServer();
    }

    private synchronized boolean connectToServer(){
        if(channel == null || !channel.isActive()) {
            try {
                ChannelFuture future = bootstrap.connect().sync();
                if (future.isSuccess()) {
                    channel = future.channel();
                    bindId();
                    InetSocketAddress socketAddress = (InetSocketAddress) channel.localAddress();
                    log.info("{}连接服务端成功！LocalPort:{};RemoteAddress:[{}:{}]",clientConfig.getName(),socketAddress.getPort(),clientConfig.getServerHost(), clientConfig.getServerPort());
                    return true;
                }
            } catch (Exception e) {

            }
            log.info("{}连接服务端失败！RemoteAddress:[{}:{}]",clientConfig.getName(),clientConfig.getServerHost(), clientConfig.getServerPort());
            reconnect();
        }else {
            log.info("{}客户端已连接，请勿重复连接！RemoteAddress:[{}:{}]",clientConfig.getName(),clientConfig.getServerHost(),clientConfig.getServerPort());
        }
        return false;
    }

    void setId(String id){
        this.id = id;
        bindId();
    }

    void unsetId(){
        this.id = null;
        if (channel != null) {
            channel.attr(AttributeKey.valueOf(CLIENT_ID)).set(null);
        }
    }

    void bindId(){
        if(id != null && !id.isEmpty()) {
            if (channel != null) {
                channel.attr(AttributeKey.valueOf(CLIENT_ID)).set(id);
            }
        }
    }

    public Channel getChannel(){
        return this.channel;
    }

    public boolean isConnected(){
        return this.channel != null && this.channel.isActive();
    }

    public ChannelFuture sendMessage(Object message){
        if(this.channel == null){
            throw new LightException(clientConfig.getName()+"服务没有启动！无法发送到RemoteAddress:["+clientConfig.getServerHost()+":"+clientConfig.getServerPort()+"]");
        }
        ChannelFuture channelFuture = this.channel.writeAndFlush(message);
        channelFuture.addListener(failMessageHandler);
        return channelFuture;
    }


    public void disconnect(){
        this.disconnected = true;
        if(this.channel != null) {
            this.channel.close();//关闭TCP连接
            this.channel = null;
        }
    }

    public void destroy(){
        if(shareGroup){
            throw new LightException("不能销毁共享的线程组，请通过管理器销毁！");
        }
        try {
            if (group != null) {
                group.shutdownGracefully().sync();
            }
        } catch (InterruptedException e) {
        }
    }

    private void reconnect(){
        if(clientConfig.getReconnectInterval() >0) {
            group.schedule(() ->{
                if(!disconnected) {
                    log.info("{}尝试重新连接到服务端！RemoteAddress:[{}:{}]", clientConfig.getName(), clientConfig.getServerHost(), clientConfig.getServerPort());
                    connectToServer();
                }
            }, clientConfig.getReconnectInterval(), TimeUnit.SECONDS);
        }
    }

    private class TcpClientHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            log.info("{}连接断开！RemoteAddress:[{}:{}]",clientConfig.getName(),clientConfig.getServerHost(), clientConfig.getServerPort());
            reconnect();
            super.channelInactive(ctx);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            if(!((cause instanceof IOException) && cause.getMessage().equals("远程主机强迫关闭了一个现有的连接。"))) {
                log.error(clientConfig.getName() + "捕获异常！RemoteAddress:["+clientConfig.getServerHost()+":"+clientConfig.getServerPort()+"]", cause);
            }
            ctx.channel().close();
            super.exceptionCaught(ctx, cause);
        }

    }

    private class TcpClientExceptionHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            //不调用super.exceptionCaught()，停止传递
        }

    }



}
