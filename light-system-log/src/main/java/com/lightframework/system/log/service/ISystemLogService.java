package com.lightframework.system.log.service;


import com.lightframework.system.log.model.SystemLog;

import java.time.LocalDate;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author yg
 * @since 2023-07-28
 */
public interface ISystemLogService {

    void createTable();

    void save(SystemLog systemLog);

    void removeLessThanCreateTime(LocalDate localDate);
}
