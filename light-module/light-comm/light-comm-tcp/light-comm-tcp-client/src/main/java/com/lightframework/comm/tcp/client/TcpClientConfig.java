package com.lightframework.comm.tcp.client;

import com.lightframework.comm.tcp.common.handler.ChannelInitializationHandler;
import com.lightframework.comm.tcp.common.heartbeat.HeartBeatConfig;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TcpClientConfig {

    private String name = "TcpClient";

    /**
     * 服务端主机地址
     */
    private String serverHost;

    /**
     * 服务端端口
     */
    private int serverPort = 8070;

    /**
     * 默认值0：使用CPU核心数*2的worker线程数
     */
    private int threadCount = 0;

    /**
     * 设置为true时，TCP会尝试检测连接的活跃性,一般如果两个小时内没有数据的通信时,TCP会自动发送一个活动探测数据报文
     */
    private boolean keepalive = true;

    /**
     * 是否不延迟
     */
    private boolean noDelay = true;

    /**
     * 重连时间间隔（秒），大于0自动重连
     */
    private int reconnectInterval = 5;

    /**
     * 读空闲检查，超过空闲时间断开连接。0：不检查
     */
    private int readerIdleTimeSeconds = 0;

    /**
     * 通道初始化
     */
    private ChannelInitializationHandler initializationHandler;

    /**
     * 心跳配置
     */
    @Setter(AccessLevel.NONE)
    private HeartBeatConfig heartBeatConfig = new HeartBeatConfig();


}
