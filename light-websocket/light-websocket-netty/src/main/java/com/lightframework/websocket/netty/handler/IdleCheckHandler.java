package com.lightframework.websocket.netty.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/*** 空闲检查
 * @author yg
 * @date 2024/6/27 18:02
 * @version 1.0
 */
@Slf4j
public class IdleCheckHandler extends IdleStateHandler {

    public IdleCheckHandler(int idleTimeSeconds) {
        super(idleTimeSeconds, 0, 0);
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) {
        if (evt.state() == IdleState.READER_IDLE) {
            ctx.channel().close();
        }
    }
}
