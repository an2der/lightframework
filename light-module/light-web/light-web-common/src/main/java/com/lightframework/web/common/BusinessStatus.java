package com.lightframework.web.common;

/*** 业务状态
 * @author yg
 * @date 2022/6/10 19:37
 * @version 1.0
 */
public interface BusinessStatus {
    Status UNKNOWN = new Status(0,"未知错误，请联系管理员");
    Status SUCCESS = new Status(200,"执行成功");
    Status FAIL = new Status(300,"执行失败");
    Status BAD_REQUEST = new Status(400,"请求参数错误");
    Status UNAUTHORIZED = new Status(401,"未登录");
    Status FORBIDDEN = new Status(403,"没有访问权限");
    Status NOT_FOUND = new Status(404,"资源不存在");
    Status ERROR = new Status(500,"系统异常，请联系管理员");

    class Status{
        private final int code;

        private final String message;

        Status(int code, String message) {
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
}
