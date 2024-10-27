package com.lightframework.starter.comm.tcp.client;

import com.lightframework.comm.tcp.client.HeartBeatBuilder;
import com.lightframework.comm.tcp.client.TcpClient;
import com.lightframework.comm.tcp.client.TcpClientConfig;
import com.lightframework.comm.tcp.common.handler.ChannelInitializationHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "tcp.client",name = "enabled",havingValue = "true",matchIfMissing = true)
public class TcpClientBuilder {

    @Autowired
    private TcpClientProperties tcpClientProperties;

    @Autowired
    private ChannelInitializationHandler channelInitializationHandler;

    @Autowired(required = false)
    private HeartBeatBuilder heartBeatBuilder;

    @Bean
    public TcpClient buildTcpClient(){
        TcpClientConfig tcpClientConfig = new TcpClientConfig();
        BeanUtils.copyProperties(tcpClientProperties,tcpClientConfig);
        BeanUtils.copyProperties(tcpClientProperties.getHeartBeatConfig(),tcpClientConfig.getHeartBeatConfig());
        tcpClientConfig.setInitializationHandler(channelInitializationHandler);
        if(heartBeatBuilder != null){
            tcpClientConfig.getHeartBeatConfig().setHeartBeatBuilder(heartBeatBuilder);
        }
        return new TcpClient(tcpClientConfig);
    }
}
