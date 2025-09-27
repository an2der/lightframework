package com.lightframework.comm.tcp.server;

import com.lightframework.comm.tcp.common.handler.ChannelInitializationHandler;
import com.lightframework.comm.tcp.common.heartbeat.HeartBeatConfig;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TcpServerConfig {

    /**
     * TcpServer名称
     */
    private String name = "TcpServer";

    /**
     * 绑定host
     */
    private String host;

    /**
     * 端口
     */
    private int port = 8070;

    /**
     * bossThread的数量设置为1就足够了，因为在一个端口上监听连接请求通常不需要并发处理
     */
    private int bossThreadCount = 1;

    /**
     * 默认值0：使用CPU核心数*2的worker线程数
     */
    private int workThreadCount = 0;

    /**
     * 设置为true时，TCP会尝试检测连接的活跃性,一般如果两个小时内没有数据的通信时,TCP会自动发送一个活动探测数据报文
     */
    private boolean keepalive = true;

    /**
     * 最大连接数， backlog的值即为未连接队列和已连接队列的和
     */
    private int backlog = 1024;

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
