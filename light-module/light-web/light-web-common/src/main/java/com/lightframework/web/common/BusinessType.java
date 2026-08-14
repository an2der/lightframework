package com.lightframework.web.common;

/*** 业务类型
 * 系统内置常用业务类型，够用时直接使用即可；
 * 若不够用，可继承本类扩展新的业务类型（注解中通过 * 常量引用编码）。
 * @author yg
 * @date 2023/7/28 11:24
 * @version 1.0
 */
public interface BusinessType {

    /** 其它 */
    int OTHER = 0;
    /** 增加 */
    int INSERT = 1;
    /** 删除 */
    int DELETE = 2;
    /** 修改 */
    int UPDATE = 3;
    /** 查询 */
    int SELECT = 4;
    /** 登录 */
    int LOGIN = 5;
    /** 登出 */
    int LOGOUT = 6;

}
