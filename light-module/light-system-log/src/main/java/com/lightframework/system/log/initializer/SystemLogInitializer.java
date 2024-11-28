package com.lightframework.system.log.initializer;

import com.lightframework.system.log.service.ISystemLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
public class SystemLogInitializer implements CommandLineRunner {

    @Autowired
    private ISystemLogService systemLogService;

    @Override
    public void run(String... args) throws Exception {
        systemLogService.createTable();
    }
}
