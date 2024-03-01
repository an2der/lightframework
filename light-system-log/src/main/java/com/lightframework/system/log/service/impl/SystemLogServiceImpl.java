package com.lightframework.system.log.service.impl;

import com.lightframework.system.log.dao.SystemLogMapper;
import com.lightframework.system.log.model.SystemLog;
import com.lightframework.system.log.service.ISystemLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Override
    public void createTable() {
        systemLogMapper.createTable();
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
