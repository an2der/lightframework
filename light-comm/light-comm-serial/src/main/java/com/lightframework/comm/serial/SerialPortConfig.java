package com.lightframework.comm.serial;

import com.fazecast.jSerialComm.SerialPort;
import lombok.Data;

@Data
public class SerialPortConfig {

    private String serialPortName;//串口名称
    private int baudRate = 9600;//波特率
    private int dataBits = 8;//数据位
    private int stopBits = SerialPort.ONE_STOP_BIT;//停止位
    private int parity = SerialPort.NO_PARITY;//校验位
    private int reconnectInterval = 5;//重连时间间隔 (秒)，大于0自动重连

    private SerialPortDataReceiver dataReceiver;

    public SerialPortConfig(String serialPortName){
        this.serialPortName = serialPortName;
    }
}
