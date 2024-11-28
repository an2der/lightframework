package com.lightframework.system.log.service.impl;

import com.lightframework.system.log.dao.SystemLogMapper;
import com.lightframework.system.log.model.SystemLog;
import com.lightframework.system.log.service.ISystemLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.time.LocalDate;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author yg
 * @since 2023-07-28
 */
@Service
public class SystemLogServiceImpl implements ISystemLogService {

    @Autowired
    private SystemLogMapper systemLogMapper;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ResourceLoader resourceLoader;

    @Override
    public void createTable() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScripts(resourceLoader.getResource("classpath:sql/system_log.sql"));
        populator.execute(dataSource);
    }

    @Override
    public void save(SystemLog systemLog) {
        systemLogMapper.insert(systemLog);
    }

    @Override
    public void removeLessThanCreateTime(LocalDate localDate) {
        systemLogMapper.deleteLessThanCreateTime(localDate);
    }
}
