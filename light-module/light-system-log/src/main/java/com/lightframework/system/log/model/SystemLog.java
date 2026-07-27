package com.lightframework.system.log.model;

import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author yg
 * @since 2023-07-28
 */
@Getter
@Setter
public class SystemLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * ip地址
     */
    private String ipAddr;

    /**
     * 请求参数
     */
    private String requestParam;

    /**
     * 执行结果（200：成功，300：失败，500：异常）
     */
    private int resultCode;

    /**
     * 操作类型（0：其它，1：新增，2：删除，3：修改，4：查询，5：登录，6：登出）
     */
    private int operationType;

    /**
     * 操作描述
     */
    private String operationDesc;

    /**
     * 是否执行成功
     */
    private boolean successful;

    /**
     * 失败原因
     */
    private String failReason;

    /**
     * 模块key
     */
    private String moduleKey;

    /**
     * 模块名
     */
    private String moduleName;


}
