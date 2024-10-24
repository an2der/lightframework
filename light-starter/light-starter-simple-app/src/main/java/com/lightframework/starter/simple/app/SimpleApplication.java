package com.lightframework.starter.simple.app;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@Slf4j
public class SimpleApplication {

    public static ConfigurableApplicationContext run(Class<?> primarySource, String... args) {
        return new SpringApplicationBuilder().sources(primarySource).web(WebApplicationType.NONE).headless(false).run(args);
    }

    /**
     * 守护进程任务，保持应用不退出
     */
    @Scheduled(fixedDelay = Integer.MAX_VALUE)
    public void daemonTask() {
    }
}
