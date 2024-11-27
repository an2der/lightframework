package com.lightframework.starter.comm.tcp.client;

import com.lightframework.comm.tcp.client.TcpClient;
import com.lightframework.util.spring.SpringContextUtil;

public class TcpClientHolder {

    private TcpClientHolder(){}
    static final String TCP_CLIENT_NAME = "tcpClient";
    private static TcpClient tcpClient = null;

    public static TcpClient getTcpClient(){
        if(tcpClient == null){
            tcpClient = SpringContextUtil.getBean(TCP_CLIENT_NAME);
        }
        return tcpClient;
    }
}
