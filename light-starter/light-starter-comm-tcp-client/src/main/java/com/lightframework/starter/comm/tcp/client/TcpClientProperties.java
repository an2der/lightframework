package com.lightframework.starter.comm.tcp.client;

import com.lightframework.comm.tcp.client.TcpClientConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "tcp-client")
public class TcpClientProperties extends TcpClientConfig {
    /**
     * 是否开启tcp client
     */
    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
