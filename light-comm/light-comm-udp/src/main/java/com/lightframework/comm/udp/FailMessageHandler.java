package com.lightframework.comm.udp;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;


@Slf4j
public class FailMessageHandler implements ChannelFutureListener {

    private String name;

    public FailMessageHandler(String name){
        this.name = name;
    }
    @Override
    public void operationComplete(ChannelFuture channelFuture) throws Exception {
        if(!channelFuture.isSuccess()){
            InetSocketAddress socketAddress = (InetSocketAddress) channelFuture.channel().remoteAddress();
            String ip = socketAddress.getAddress().getHostAddress();
            int port = socketAddress.getPort();
            log.error(name+"服务发送消息失败，address：["+ip+":"+port+"]，cause：" + channelFuture.cause().getMessage(),channelFuture.cause());
        }
    }
}
