package com.lightframework.starter.comm.tcp.server;

import com.lightframework.comm.tcp.server.TcpServerManager;
import com.lightframework.util.spring.SpringBeanUtil;

public class TcpServerManagerHolder {

    private TcpServerManagerHolder(){}
    static final String TCP_SERVER_MANAGER_NAME = "tcpServerManager";
    private static TcpServerManager tcpServerManager = null;

    public static TcpServerManager getTcpServerManager(){
        if(tcpServerManager == null){
            tcpServerManager = (TcpServerManager) SpringBeanUtil.getBean(TCP_SERVER_MANAGER_NAME);
        }
        return tcpServerManager;
    }
}
