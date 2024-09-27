package com.lightframework.comm.tcp.client;

import io.netty.channel.socket.SocketChannel;

public interface ChannelInitializationHandler {
    void initChannel(SocketChannel socketChannel) throws Exception;
}
