package com.lightframework.starter.comm.tcp.client;

import com.lightframework.comm.tcp.client.ChannelInitializationHandler;
import com.lightframework.comm.tcp.client.TcpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "tcp-client",name = "enabled",havingValue = "true",matchIfMissing = true)
public class TcpClientBuilder {

    @Autowired
    private TcpClientProperties tcpClientProperties;

    @Autowired
    private ChannelInitializationHandler channelInitializationHandler;

    @Bean
    public TcpClient buildTcpClient(){
        tcpClientProperties.setInitializationHandler(channelInitializationHandler);
        return new TcpClient(tcpClientProperties);
    }
}
