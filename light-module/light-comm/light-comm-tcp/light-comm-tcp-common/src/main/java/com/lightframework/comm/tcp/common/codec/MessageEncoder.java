package com.lightframework.comm.tcp.common.codec;

import com.lightframework.comm.tcp.common.constant.MessageConstants;
import com.lightframework.comm.tcp.common.model.Message;
import com.lightframework.util.serialize.ProtostuffUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MessageEncoder extends MessageToByteEncoder<Message> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Message message, ByteBuf byteBuf){
        byte[] bytes = ProtostuffUtil.serialize(message);
        byte[] delimiter = MessageConstants.DELIMITER;
        byte[] total = new byte[bytes.length + delimiter.length];
        System.arraycopy(bytes, 0, total, 0, bytes.length);
        System.arraycopy(delimiter, 0, total, bytes.length, delimiter.length);
        byteBuf.writeBytes(total);
    }
}