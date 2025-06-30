package com.lightframework.starter.comm.tcp.server;

import com.lightframework.comm.tcp.common.handler.ChannelInitializationHandler;
import com.lightframework.comm.tcp.server.TcpServer;
import com.lightframework.comm.tcp.server.TcpServerConfig;
import com.lightframework.util.spring.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "tcp.server",name = "enabled",havingValue = "true",matchIfMissing = true)
@Slf4j
public class TcpServerStarter implements ApplicationRunner {

    @Autowired
    private TcpServerProperties tcpServerProperties;

    @Autowired
    private ChannelInitializationHandler channelInitializationHandler;

    private TcpServer tcpServer;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try{
            TcpServerConfig tcpServerConfig = new TcpServerConfig();
            BeanUtils.copyProperties(tcpServerProperties,tcpServerConfig);
            tcpServerConfig.setInitializationHandler(channelInitializationHandler);
            tcpServer = TcpServer.start(tcpServerConfig);
            SpringContextUtil.registerBean(TcpServerManagerHolder.TCP_SERVER_MANAGER_NAME,tcpServer.getTcpServerManager());
        }catch (Exception e){
            log.error(e.getMessage(),e.getCause());
            SpringContextUtil.exit();
        }
    }
}
