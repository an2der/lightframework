package com.lightframework.comm.tcp.common.handler;

import com.lightframework.comm.tcp.common.codec.MessageDecoder;
import com.lightframework.comm.tcp.common.codec.MessageEncoder;
import com.lightframework.comm.tcp.common.constant.MessageConstants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

public class MessageInitializationHandler implements ChannelInitializationHandler{

    //每一帧最大长度
    private final int maxFrameLength;

    private final AbstractMessageHandler abstractMessageHandler;

    private final ByteBuf delimiter = Unpooled.wrappedBuffer(MessageConstants.DELIMITER);

    public MessageInitializationHandler(AbstractMessageHandler abstractMessageHandler){
        this.maxFrameLength = 8 * 1024 * 1024;//默认8M
        this.abstractMessageHandler = abstractMessageHandler;
    }
    public MessageInitializationHandler(int maxFrameLength,AbstractMessageHandler abstractMessageHandler){
        this.maxFrameLength = maxFrameLength;
        this.abstractMessageHandler = abstractMessageHandler;
    }

    @Override
    public void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline().addLast(new DelimiterBasedFrameDecoder(maxFrameLength,delimiter));
        socketChannel.pipeline().addLast(new MessageDecoder());
        socketChannel.pipeline().addLast(new MessageEncoder());
        socketChannel.pipeline().addLast(abstractMessageHandler);
    }
}
