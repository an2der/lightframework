package com.lightframework.comm.serial;

/** 串口数据接收
 * @Author yg
 * @Date 2024/10/8 10:01
 */
public interface SerialPortDataReceiver {
    void receive(byte [] data);
}
