package com.lightframework.system.log.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lightframework.system.log.model.SystemLog;
import com.lightframework.system.log.properties.SystemLogProperties;
import com.lightframework.system.log.service.ISystemLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ClearLogScheduler {

    @Autowired
    private SystemLogProperties systemLogProperties;

    @Autowired
    private ISystemLogService systemLogService;

    @Scheduled(cron = "0 0 1 * * ?")
    public void clearLog(){
        LocalDate localDate = LocalDate.now();
        localDate.minusDays(systemLogProperties.getReservedDays());
        systemLogService.remove(new LambdaQueryWrapper<SystemLog>().lt(SystemLog::getCreateTime,localDate));
    }
}
