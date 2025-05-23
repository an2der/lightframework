package com.lightframework.starter.comm.udp;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "udp")
@Getter
@Setter
public class UdpSocketProperties {
    /**
     * 是否开启udp socket
     */
    private boolean enabled = true;

    /**
     * UdpSocket名称
     */
    private String name = "UdpSocket";

    /**
     * 绑定host
     */
    private String host;

    /**
     * 端口 0代表随机端口
     */
    private int port = 0;

    /**
     * 默认值0：使用CPU核心数*2的worker线程数
     */
    private int workThreadCount = 0;

    /**
     * 重用地址
     */
    private boolean reuseaddr = true;

    /**
     * 每帧数据最大长度
     */
    private int maxFrameLength = 2048;

}
