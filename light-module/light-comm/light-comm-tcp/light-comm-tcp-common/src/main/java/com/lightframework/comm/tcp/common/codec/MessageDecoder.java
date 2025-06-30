package com.lightframework.comm.tcp.common.codec;

import com.lightframework.comm.tcp.common.model.Message;
import com.lightframework.util.serialize.ProtostuffUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class MessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> list) {
        try {
            byte[] body = new byte[in.readableBytes()];
            in.readBytes(body);
            Message message = ProtostuffUtil.deserialize(body);
            list.add(message);
        } catch (Exception e) {
            log.error("netty消息解码器发生异常",e);
        }
    }
}