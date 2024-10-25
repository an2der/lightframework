package com.lightframework.starter.comm.tcp.client;

import com.lightframework.comm.tcp.client.TcpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "tcp-client",name = "enabled",havingValue = "true",matchIfMissing = true)
public class TcpClientRunner implements CommandLineRunner {

    @Autowired
    private TcpClient tcpClient;

    @Override
    public void run(String... args) throws Exception {
        tcpClient.connect();
    }
}
