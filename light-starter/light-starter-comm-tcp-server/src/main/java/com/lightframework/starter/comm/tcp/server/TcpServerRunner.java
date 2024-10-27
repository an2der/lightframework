package com.lightframework.starter.comm.tcp.server;

import com.lightframework.comm.tcp.common.handler.ChannelInitializationHandler;
import com.lightframework.comm.tcp.server.TcpServer;
import com.lightframework.comm.tcp.server.TcpServerConfig;
import com.lightframework.util.spring.SpringBeanUtil;
import com.lightframework.util.spring.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "tcp.server",name = "enabled",havingValue = "true",matchIfMissing = true)
@Slf4j
public class TcpServerRunner implements CommandLineRunner {

    @Autowired
    private TcpServerProperties tcpServerProperties;

    @Autowired
    private ChannelInitializationHandler channelInitializationHandler;

    private TcpServer tcpServer;

    @Override
    public void run(String... args) {
        try{
            TcpServerConfig tcpServerConfig = new TcpServerConfig();
            BeanUtils.copyProperties(tcpServerProperties,tcpServerConfig);
            tcpServerConfig.setInitializationHandler(channelInitializationHandler);
            tcpServer = TcpServer.start(tcpServerConfig);
            SpringBeanUtil.registerBean("tcpServerManager",tcpServer.getTcpServerManager());
        }catch (Exception e){
            log.error(e.getMessage(),e.getCause());
            SpringContextUtil.exit();
        }
    }

}
