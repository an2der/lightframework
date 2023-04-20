package com.lightframework.common;

/** 业务异常
 * @author yg
 * @date 2022/6/10 10:42
 * @version 1.0
 */
public class BusinessException extends RuntimeException{

    private int code;
    private String message;

    public BusinessException(String message){
        this(BusinessStatus.FAIL.getCode(), message,null);
    }

    public BusinessException(BusinessStatus status){
        this(status.getCode(), status.getMessage(),null);
    }

    public BusinessException(Throwable throwable){
        this(BusinessStatus.FAIL.getCode(),throwable.getMessage(),throwable);
    }

    public BusinessException(int code, String message){
        this(code,message,null);
    }

    public BusinessException(String message, Throwable throwable){
        this(BusinessStatus.FAIL.getCode(),message,throwable);
    }

    public BusinessException(int code, String message, Throwable throwable){
        super(message,throwable);
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
