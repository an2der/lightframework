package com.lightframework.system.log.dao;

import com.lightframework.system.log.model.SystemLog;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;

import java.time.LocalDate;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author yg
 * @since 2023-07-28
 */
public interface SystemLogMapper {

    @Insert("INSERT INTO `t_system_log` " +
            "(`id`, `user_id`, `username`, `create_time`, `ip_addr`, `request_param`, `result_code`, `operation_type`, `operation_desc`, `successful`, `fail_reason`, `module_key`, `module_name`)" +
            " VALUES (#{id}, #{userId}, #{username}, #{createTime}, #{ipAddr}, #{requestParam}, #{resultCode}, #{operationType}, #{operationDesc}, #{successful}, #{failReason}, #{moduleKey}, #{moduleName})")
    void insert(SystemLog systemLog);

    @Delete("DELETE FROM t_system_log WHERE create_time < #{localDate}")
    void deleteLessThanCreateTime(LocalDate localDate);

}
