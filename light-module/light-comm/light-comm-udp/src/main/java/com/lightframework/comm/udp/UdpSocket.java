package com.lightframework.comm.udp;

import com.lightframework.common.LightException;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * tcp server
 */
@Slf4j
public class UdpSocket {

    private UdpSocketConfig udpSocketConfig;

    private EventLoopGroup group;

    private Channel channel;

    private FailMessageHandler failMessageHandler;

    private UdpSocket(){}

    public static UdpSocket start(UdpSocketConfig udpSocketConfig) {
        UdpSocket udpSocket = new UdpSocket();
        udpSocket.udpSocketConfig = udpSocketConfig;
        udpSocket.failMessageHandler = new FailMessageHandler(udpSocketConfig.getName());
        udpSocket.start();
        return udpSocket;
    }

    private void start() {
        try {
            group = new NioEventLoopGroup(udpSocketConfig.getWorkThreadCount());
            Bootstrap bootstrap = new Bootstrap();
            InetSocketAddress inetSocketAddress = udpSocketConfig.getHost() != null && !udpSocketConfig.getHost().isEmpty()
                    ?new InetSocketAddress(udpSocketConfig.getHost(), udpSocketConfig.getPort())
                    :new InetSocketAddress(udpSocketConfig.getPort());
            bootstrap.group(group)
                    .localAddress(inetSocketAddress)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .option(ChannelOption.SO_REUSEADDR, udpSocketConfig.isReuseaddr())
                    .option(ChannelOption.RCVBUF_ALLOCATOR,new FixedRecvByteBufAllocator(udpSocketConfig.getMaxFrameLength()))
                    .handler(new ChannelInitializer<NioDatagramChannel>() {
                        @Override
                        protected void initChannel(NioDatagramChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(udpSocketConfig.getDatagramReceiver());
                            socketChannel.pipeline().addFirst(new UdpSocketHandler());
                            socketChannel.pipeline().addLast(new UdpSocketExceptionHandler());
                        }
                    });
            ChannelFuture future = bootstrap.bind().sync();
            if (future.isSuccess()) {
                this.channel = future.channel();
                InetSocketAddress socketAddress = (InetSocketAddress) channel.localAddress();
                log.info("{}服务启动成功！IP:{},PORT:{}", udpSocketConfig.getName(),inetSocketAddress.getAddress().getHostAddress(), socketAddress.getPort());
            } else {
                throw new Exception(future.cause());
            }
        } catch (Exception e) {
            log.info(udpSocketConfig.getName() +"服务启动失败！");
            throw new LightException(udpSocketConfig.getName() + "服务启动时发生异常",e);
        }
    }

    public ChannelFuture sendMessage(byte [] message,String recipientHost,int recipientPort){
        return sendMessage(message,new InetSocketAddress(recipientHost,recipientPort));
    }

    public ChannelFuture sendMessage(byte [] message,InetSocketAddress recipient){
        if(this.channel == null){
            throw new LightException(udpSocketConfig.getName()+"服务没有启动！");
        }
        DatagramPacket packet = new DatagramPacket(Unpooled.wrappedBuffer(message),recipient);
        ChannelFuture channelFuture = this.channel.writeAndFlush(packet);
        channelFuture.addListener(failMessageHandler);
        return channelFuture;
    }


    public void shutdown(){
        try {
            if (group != null) {
                group.shutdownGracefully().sync();
            }
        } catch (InterruptedException e) {

        }
    }

    private class UdpSocketHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
            String ip = socketAddress.getAddress().getHostAddress();
            int port = socketAddress.getPort();
            log.error(udpSocketConfig.getName() + "捕获异常，address：["+ip+":"+port+"]，cause：" + cause.getMessage(), cause);
            ctx.channel().close();
            super.exceptionCaught(ctx, cause);
        }
    }

    private class UdpSocketExceptionHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            //不调用super.exceptionCaught()，停止传递
        }
    }

}
