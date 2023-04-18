package com.lightframework.web.common;

/*** 业务状态
 * @author yg
 * @date 2022/6/10 19:37
 * @version 1.0
 */
public enum BusinessStatus {
    UNKNOWN(0,"未知错误，请联系管理员"),
    SUCCESS(200,"执行成功"),
    FAIL(300,"执行失败"),
    UNAUTHORIZED(401,"未登录"),
    FORBIDDEN(403,"没有访问权限"),
    NOT_FOUND(404,"资源不存在"),
    ERROR(500,"系统异常，请联系管理员");

    private int code;

    private String message;

    BusinessStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
