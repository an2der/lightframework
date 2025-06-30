package com.lightframework.starter.comm.udp;

import com.lightframework.comm.udp.AbstractDatagramReceiver;
import com.lightframework.comm.udp.UdpSocket;
import com.lightframework.comm.udp.UdpSocketConfig;
import com.lightframework.util.spring.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "udp",name = "enabled",havingValue = "true",matchIfMissing = true)
@Slf4j
public class UdpSocketStarter implements ApplicationRunner {

    @Autowired
    private UdpSocketProperties udpSocketProperties;

    @Autowired
    private AbstractDatagramReceiver datagramReceiver;

    private UdpSocket udpSocket;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try{
            UdpSocketConfig udpSocketConfig = new UdpSocketConfig();
            BeanUtils.copyProperties(udpSocketProperties,udpSocketConfig);
            udpSocketConfig.setDatagramReceiver(datagramReceiver);
            udpSocket = UdpSocket.start(udpSocketConfig);
            SpringContextUtil.registerBean(UdpSocketHolder.UDP_SOCKET_NAME,udpSocket);
        }catch (Exception e){
            log.error(e.getMessage(),e.getCause());
            SpringContextUtil.exit();
        }
    }
}
