package com.lightframework.system.log.service.impl;

import com.lightframework.system.log.model.SystemLog;
import com.lightframework.system.log.dao.SystemLogMapper;
import com.lightframework.system.log.service.ISystemLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author yg
 * @since 2023-07-28
 */
@Service
public class SystemLogServiceImpl extends ServiceImpl<SystemLogMapper, SystemLog> implements ISystemLogService {

}
