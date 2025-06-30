package com.lightframework.comm.tcp.client;

import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class HeartBeatHandler {

    private TcpClientConfig tcpClientConfig;

    public static void handle(SocketChannel channel, TcpClientConfig tcpClientConfig){
        if(tcpClientConfig.getHeartBeatConfig().getHeartBeatInterval() > 0) {
            HeartBeatHandler heartBeatHandler = new HeartBeatHandler();
            heartBeatHandler.tcpClientConfig = tcpClientConfig;
            if (tcpClientConfig.getHeartBeatConfig().isFixedCycleSend()) {
                channel.pipeline().addLast(heartBeatHandler.new FixedCycleHeartBeatHandler());
            } else {
                channel.pipeline().addLast(heartBeatHandler.new IdleHeartBeatHandler());
            }
        }
    }

    private HeartBeatHandler() {

    }

    public void sendHeartBeat(Channel channel){
        if(channel.isActive()) {
            channel.writeAndFlush(tcpClientConfig.getHeartBeatConfig().getHeartBeatBuilder().build()).addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future) {
                    if (future.isSuccess()) {
                        log.info(tcpClientConfig.getName() + "向服务端发送心跳成功");
                    } else {
                        log.info(tcpClientConfig.getName() + "向服务端发送心跳失败：[{}]", future.cause().getMessage());
                        future.channel().close();
                    }
                }
            });
        }
    }

    private class FixedCycleHeartBeatHandler extends ChannelInboundHandlerAdapter{

        private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            scheduler.scheduleAtFixedRate(()->{
                sendHeartBeat(ctx.channel());
            }, 0, tcpClientConfig.getHeartBeatConfig().getHeartBeatInterval(), TimeUnit.SECONDS);
            super.channelActive(ctx);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            scheduler.shutdownNow();
            super.channelInactive(ctx);
        }
    }

    private class IdleHeartBeatHandler extends IdleStateHandler{

        public IdleHeartBeatHandler() {
            super(0, tcpClientConfig.getHeartBeatConfig().getHeartBeatInterval(), 0);
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
