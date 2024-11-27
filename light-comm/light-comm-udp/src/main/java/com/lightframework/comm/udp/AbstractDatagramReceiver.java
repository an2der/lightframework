package com.lightframework.comm.udp;

import io.netty.channel.ChannelHandler;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

@ChannelHandler.Sharable
public abstract class AbstractDatagramReceiver extends SimpleChannelInboundHandler<DatagramPacket> {
}
