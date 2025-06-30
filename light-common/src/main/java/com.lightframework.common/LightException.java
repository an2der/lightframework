package com.lightframework.common;

/** 异常类
 * @Author yg
 * @Date 2024/11/28 14:16
 */
public class LightException extends RuntimeException{

    public LightException(){
        super();
    }

    public LightException(String message){
        super(message);
    }

    public LightException(Throwable throwable){
        super(throwable.getMessage(),throwable);
    }

    public LightException(String message, Throwable throwable){
        super(message,throwable);
    }
}
