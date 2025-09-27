package com.lightframework.comm.tcp.common.heartbeat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeartBeatConfig {

    /**
     * 心跳间隔（秒），大于0发送心跳
     */
    private int heartBeatInterval = 0;

    /**
     * 是否固定按固定心跳周期发送
     * 否：在客户端心跳间隔时间内没有向服务端发送任何数据时发送心跳
     */
    private boolean fixedCycleSend = false;

    /**
     * 构建心跳
     */
    private HeartBeatBuilder heartBeatBuilder = () -> new byte[0];
}
