package com.lightframework.starter.comm.tcp.common;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "tcp.message-codec")
@Getter
@Setter
public class TcpMessageCodecProperties {

    /**
     * 默认消息解码器每帧最大长度设置，默认8M
     */
    private int maxFrameLength = 8388608;
}
