package com.lightframework.starter.comm.tcp.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "tcp.client")
@Getter
@Setter
public class TcpClientProperties {
    /**
     * 是否开启tcp client
     */
    private boolean enabled = true;

    /**
     * 客户端名称
     */
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
     * 心跳配置
     */
    private HeartBeatConfig heartBeatConfig = new HeartBeatConfig();

    /**
     * 心跳配置对象
     */
    @Getter
    @Setter
    public static class HeartBeatConfig{

        /**
         * 心跳间隔（秒），大于0发送心跳
         */
        private int heartBeatInterval = 0;

        /**
         * 是否固定按固定心跳周期发送
         * 否：在客户端心跳间隔时间内没有向服务端发送任何数据时发送心跳
         */
        private boolean fixedCycleSend = false;

    }

}
