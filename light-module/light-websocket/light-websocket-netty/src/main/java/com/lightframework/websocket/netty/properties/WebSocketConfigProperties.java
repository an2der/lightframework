package com.lightframework.websocket.netty.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/*** websocket配置
 * @author yg
 * @date 2024/5/24 9:23
 * @version 1.0
 */
@Component
@ConfigurationProperties(prefix = "websocket")
@Getter
@Setter
public class WebSocketConfigProperties {

    /**
     * websocket绑定host
     */
    private String host;

    /**
     * websocket端口
     */
    private int port = 8090;

    /**
     * websocket路径
     */
    private String websocketPath = "/websocket";

    /**
     * bossThread的数量设置为1就足够了，因为在一个端口上监听连接请求通常不需要并发处理
     */
    private int bossThreadCount = 1;

    /**
     * 0: 默认使用CPU核心数*2的worker线程数
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
     * 设置单次请求内容最大大小，默认64K
     */
    private int maxContentLength = 1024 * 64;

}
