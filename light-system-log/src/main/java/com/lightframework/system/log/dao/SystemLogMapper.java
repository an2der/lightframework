package com.lightframework.system.log.dao;

import com.lightframework.system.log.model.SystemLog;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;

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
            "(`id`, `user_id`, `username`, `create_time`, `ip_addr`, `request_param`, `execute_result`, `operation_type`, `operation_desc`, `module_key`, `module_name`)" +
            " VALUES (#{id}, #{userId}, #{username}, #{createTime}, #{ipAddr}, #{requestParam}, #{executeResult}, #{operationType}, #{operationDesc}, #{moduleKey}, #{moduleName})")
    void insert(SystemLog systemLog);

    @Delete("DELETE FROM t_system_log WHERE create_time < #{localDate}")
    void deleteLessThanCreateTime(LocalDate localDate);

    @Update("CREATE TABLE IF NOT EXISTS `t_system_log` (\n" +
            "  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',\n" +
            "  `user_id` varchar(64) DEFAULT NULL COMMENT '用户id',\n" +
            "  `username` varchar(32) DEFAULT NULL COMMENT '用户名',\n" +
            "  `create_time` datetime DEFAULT NULL COMMENT '创建时间',\n" +
            "  `ip_addr` varchar(15) DEFAULT NULL COMMENT 'ip地址',\n" +
            "  `request_param` text DEFAULT NULL COMMENT '请求参数',\n" +
            "  `execute_result` int(3) DEFAULT NULL COMMENT '执行结果（200：成功，300：失败，500：异常）',\n" +
            "  `operation_type` int(3) DEFAULT NULL COMMENT '操作类型（0：其它，1：新增，2：删除，3：修改，4：查询，5：登录，6：登出）',\n" +
            "  `operation_desc` varchar(255) DEFAULT NULL COMMENT '操作描述',\n" +
            "  `module_key` varchar(64) DEFAULT NULL COMMENT '模块key',\n" +
            "  `module_name` varchar(255) DEFAULT NULL COMMENT '模块名',\n" +
            "  PRIMARY KEY (`id`)\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8")
    void createTable();
}
