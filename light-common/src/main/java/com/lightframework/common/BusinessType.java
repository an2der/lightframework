package com.lightframework.common;

/*** 业务类型
 * @author yg
 * @date 2023/7/28 11:24
 * @version 1.0
 */
public enum BusinessType {

    OTHER(0,"其它"),
    INSERT(1,"增加"),
    DELETE(2,"删除"),
    UPDATE(3,"修改"),
    SELECT(4,"查询"),
    LOGIN(5,"登录"),
    LOGOUT(6,"登出");

    private int code;

    private String desc;

    BusinessType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
