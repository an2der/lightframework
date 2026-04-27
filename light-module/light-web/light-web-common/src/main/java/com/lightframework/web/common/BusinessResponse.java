package com.lightframework.web.common;

import java.io.Serializable;

/** 业务响应
 * @author yg
 * @date 2022/6/10 18:44
 * @version 1.0
 */
public class BusinessResponse implements Serializable {

    private static final long serialVersionUID = -4651321568722805550L;

    private int code;

    private String msg;

    private Object data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public BusinessResponse(BusinessException e){
        this(e.getCode(),e.getMessage(),e.getData());
    }

    public BusinessResponse(BusinessStatus status){
        this(status.getCode(),status.getMessage(),null);
    }

    public BusinessResponse(Object data){
        this(BusinessStatus.SUCCESS.getCode(), BusinessStatus.SUCCESS.getMessage(),data);
    }

    public BusinessResponse(int code,String msg){
        this(code, msg,null);
    }

    public BusinessResponse(String msg, Object data) {
        this(BusinessStatus.SUCCESS.getCode(), msg,data);
    }

    public BusinessResponse(int code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public BusinessResponse(){
        this(BusinessStatus.SUCCESS.getCode(),BusinessStatus.SUCCESS.getMessage(),null);
    }

    public void throwBusinessException(){
        throw new BusinessException(this.code,this.msg);
    }
}
