package com.lightframework.comm.tcp.server;

import io.netty.channel.socket.SocketChannel;

public interface ChannelInitializationHandler {
    void initChannel(SocketChannel socketChannel) throws Exception;
}
