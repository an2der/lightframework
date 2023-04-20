package com.lightframework.common;

import java.io.Serializable;

/** 业务响应
 * @author yg
 * @date 2022/6/10 18:44
 * @version 1.0
 */
public class BusinessResponse implements Serializable {

    private static final long serialVersionUID = -4651321568722805550L;

    private int code;

    private String message;

    private Object data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public BusinessResponse(BusinessException e){
        this(e.getCode(),e.getMessage(),null);
    }

    public BusinessResponse(BusinessStatus status){
        this(status.getCode(),status.getMessage(),null);
    }

    public BusinessResponse(Object data){
        this(BusinessStatus.SUCCESS.getCode(), BusinessStatus.SUCCESS.getMessage(),data);
    }

    public BusinessResponse(int code,String message){
        this(code,message,null);
    }

    public BusinessResponse(String message, Object data) {
        this(BusinessStatus.SUCCESS.getCode(),message,data);
    }

    public BusinessResponse(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
}
