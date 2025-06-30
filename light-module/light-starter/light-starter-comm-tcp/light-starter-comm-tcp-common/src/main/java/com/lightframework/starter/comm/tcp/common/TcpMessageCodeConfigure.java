package com.lightframework.starter.comm.tcp.common;

import com.lightframework.comm.tcp.common.handler.AbstractMessageHandler;
import com.lightframework.comm.tcp.common.handler.ChannelInitializationHandler;
import com.lightframework.comm.tcp.common.handler.MessageInitializationHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnMissingBean(ChannelInitializationHandler.class)
public class TcpMessageCodeConfigure {

    @Autowired
    private TcpMessageCodecProperties tcpMessageCodecProperties;

    @Autowired
    private AbstractMessageHandler abstractMessageHandler;

    @Bean
    public MessageInitializationHandler messageInitializationHandler(){
        return new MessageInitializationHandler(tcpMessageCodecProperties.getMaxFrameLength(),abstractMessageHandler);
    }
}
