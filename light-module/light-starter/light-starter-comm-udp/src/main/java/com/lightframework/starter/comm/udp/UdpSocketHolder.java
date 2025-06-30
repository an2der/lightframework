package com.lightframework.starter.comm.udp;

import com.lightframework.comm.udp.UdpSocket;
import com.lightframework.util.spring.SpringContextUtil;

public class UdpSocketHolder {

    private UdpSocketHolder(){}
    static final String UDP_SOCKET_NAME = "udpSocketManager";
    private static UdpSocket udpSocket = null;

    public static UdpSocket getUdpSocket(){
        if(udpSocket == null){
            udpSocket = SpringContextUtil.getBean(UDP_SOCKET_NAME);
        }
        return udpSocket;
    }
}
