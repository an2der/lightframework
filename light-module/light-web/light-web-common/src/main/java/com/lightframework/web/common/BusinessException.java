package com.lightframework.web.common;

import com.lightframework.common.LightException;

/** 业务异常
 * @author yg
 * @date 2022/6/10 10:42
 * @version 1.0
 */
public class BusinessException extends LightException {

    private int code;

    private Object data;

    public BusinessException(String message){
        this(message,null);
    }

    public BusinessException(Object data){
        this(BusinessStatus.FAIL.getCode(),null, data,null);
    }

    public BusinessException(BusinessStatus status){
        this(status.getCode(), status.getMessage());
    }

    public BusinessException(Throwable throwable){
        this(throwable.getMessage(),throwable);
    }

    public BusinessException(int code, String message){
        this(code,message,null,null);
    }

    public BusinessException(String message, Throwable throwable){
        this(BusinessStatus.FAIL.getCode(),message, null,throwable);
    }

    public BusinessException(int code, String message,Object data, Throwable throwable){
        super(message,throwable);
        this.code = code;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public Object getData() {
        return data;
    }

}
