package com.lightframework.comm.tcp.common.heartbeat;

import cn.hutool.json.JSONUtil;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class HeartBeatHandler {

    private String name;

    private HeartBeatConfig heartBeatConfig;

    private EventLoopGroup group;

    public static void handle(SocketChannel channel, String name, EventLoopGroup group,HeartBeatConfig heartBeatConfig){
        if(heartBeatConfig.getHeartBeatInterval() > 0) {
            HeartBeatHandler heartBeatHandler = new HeartBeatHandler();
            heartBeatHandler.heartBeatConfig = heartBeatConfig;
            heartBeatHandler.name = name;
            heartBeatHandler.group = group;
            if (heartBeatConfig.isFixedCycleSend()) {
                channel.pipeline().addFirst(heartBeatHandler.new FixedCycleHeartBeatHandler());
            } else {
                channel.pipeline().addFirst(heartBeatHandler.new IdleHeartBeatHandler());
            }
        }
    }

    private HeartBeatHandler() {

    }

    private void sendHeartBeat(Channel channel){
        if(channel.isActive()) {
            Object heartbeat = heartBeatConfig.getHeartBeatBuilder().build();
            channel.writeAndFlush(heartbeat).addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future) {
                    if (future.isSuccess()) {
                        if(log.isDebugEnabled()) {
                            InetSocketAddress socketAddress = (InetSocketAddress) channel.remoteAddress();
                            String targetHost = socketAddress.getAddress().getHostAddress();
                            int targetPort = socketAddress.getPort();
                            log.debug("{}发送心跳成功，目标地址 {}:{}，数据:{}", name,targetHost, targetPort, JSONUtil.toJsonStr(heartbeat));
                        }
                    } else {
                        InetSocketAddress socketAddress = (InetSocketAddress) channel.remoteAddress();
                        String host = socketAddress.getAddress().getHostAddress();
                        int port = socketAddress.getPort();
                        log.error(String.format("%s发送心跳失败，目标地址 %s:%s，数据:%s", name, host, port, JSONUtil.toJsonStr(heartbeat)),future.cause());
                        future.channel().close();
                    }
                }
            });
        }
    }

    private class FixedCycleHeartBeatHandler extends ChannelInboundHandlerAdapter{

        private ScheduledFuture<?> scheduledFuture;

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            scheduledFuture = group.scheduleAtFixedRate(()->{
                sendHeartBeat(ctx.channel());
            }, 0, heartBeatConfig.getHeartBeatInterval(), TimeUnit.SECONDS);
            super.channelActive(ctx);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            scheduledFuture.cancel( true);
            super.channelInactive(ctx);
        }
    }

    private class IdleHeartBeatHandler extends IdleStateHandler{

        public IdleHeartBeatHandler() {
            super(0, heartBeatConfig.getHeartBeatInterval(), 0);
        }

        @Override
        protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
            if (evt.state() == IdleState.WRITER_IDLE) {
                sendHeartBeat(ctx.channel());
            }
            super.channelIdle(ctx, evt);
        }


    }
}
