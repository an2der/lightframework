package com.lightframework.starter.comm.tcp.server;

import com.lightframework.comm.tcp.server.TcpServerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "tcp-server")
public class TcpServerProperties extends TcpServerConfig {
    /**
     * 是否开启tcp server
     */
    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
