package com.lightframework.comm.tcp.common.handler;

import com.lightframework.comm.tcp.common.model.Message;
import io.netty.channel.ChannelHandler;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public abstract class AbstractMessageHandler extends SimpleChannelInboundHandler<Message> {
}
