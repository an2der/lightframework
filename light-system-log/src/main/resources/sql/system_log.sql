-- ----------------------------
-- Table structure for t_system_log
-- ----------------------------
CREATE TABLE IF NOT EXISTS `t_system_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` varchar(64) DEFAULT NULL COMMENT '用户id',
  `username` varchar(32) DEFAULT NULL COMMENT '用户名',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `ip_addr` varchar(15) DEFAULT NULL COMMENT 'ip地址',
  `request_param` text DEFAULT NULL COMMENT '请求参数',
  `execute_result` int(3) DEFAULT NULL COMMENT '执行结果（200：成功，300：失败，500：异常）',
  `operation_type` int(3) DEFAULT NULL COMMENT '操作类型（0：其它，1：新增，2：删除，3：修改，4：查询，5：登录，6：登出）',
  `operation_desc` varchar(255) DEFAULT NULL COMMENT '操作描述',
  `module_key` varchar(64) DEFAULT NULL COMMENT '模块key',
  `module_name` varchar(255) DEFAULT NULL COMMENT '模块名',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
