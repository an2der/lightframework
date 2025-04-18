package com.lightframework.comm.serial;

import cn.hutool.core.thread.ThreadUtil;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @Author yg
 * @Date 2024-09-20 10:42:09
 **/
@Slf4j
public class SerialPortManager {
    private SerialPort comPort;

    private SerialPortConfig serialPortConfig;

    private volatile boolean closed = false;

    public SerialPortManager(SerialPortConfig serialPortConfig){
        this.serialPortConfig = serialPortConfig;
        comPort = SerialPort.getCommPort(serialPortConfig.getSerialPortName());
        comPort.setBaudRate(serialPortConfig.getBaudRate());//波特率
        comPort.setNumDataBits(serialPortConfig.getDataBits());//数据位
        comPort.setNumStopBits(serialPortConfig.getStopBits());//停止位
        comPort.setParity(serialPortConfig.getParity());//校验位
        comPort.addDataListener(new SerialPortListener());
    }

    public boolean open(){
        closed = false;
        return open(false);
    }

    /**
     *
     * @param isReconnect 是不是重新连接的方式
     * @return
     */
    private synchronized boolean open(boolean isReconnect){
        if (!comPort.isOpen()){
            if(comPort.openPort()) {
                log.info("串口：{} 开启成功！",serialPortConfig.getSerialPortName());
                return true;
            } else {
                log.info("串口：{} 开启失败！",serialPortConfig.getSerialPortName());
                if(!isReconnect) {
                    reconnect();
                }
                return false;
            }
        }else {
            log.info("串口：{} 已经开启，请勿重复开启！",serialPortConfig.getSerialPortName());
            return true;
        }
    }

    public boolean close(){
        closed = true;
        if(comPort.isOpen()){
            if(comPort.closePort()){
                log.info("串口：{} 关闭成功！",serialPortConfig.getSerialPortName());
                return true;
            }else {
                log.info("串口：{} 关闭失败！",serialPortConfig.getSerialPortName());
            }
        }
        return false;
    }

    public boolean isOpen(){
        return comPort.isOpen();
    }

    public void write(byte[] data){
        if(comPort.isOpen()){
            comPort.writeBytes(data,data.length);
        }
    }

    private void reconnect(){
        if(!closed && serialPortConfig.getReconnectInterval() > 0){
            ThreadUtil.execute(() -> {
                while (!closed && !comPort.isOpen()){
                    try {
                        TimeUnit.SECONDS.sleep(serialPortConfig.getReconnectInterval());
                        if(!closed) {
                            log.info("串口：{} 开始重连...", serialPortConfig.getSerialPortName());
                            if (open(true)) {
                                break;
                            }
                        }
                    } catch (InterruptedException e) {
                        log.error("串口："+serialPortConfig.getSerialPortName()+" 重连时发生异常！",e);
                    }
                }
            });
        }
    }

    private class SerialPortListener implements SerialPortDataListener{

        @Override
        public int getListeningEvents() {
            return SerialPort.LISTENING_EVENT_DATA_AVAILABLE | SerialPort.LISTENING_EVENT_PORT_DISCONNECTED;
        }

        @Override
        public void serialEvent(SerialPortEvent serialPortEvent) {
            if(serialPortEvent.getEventType() == SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
                byte[] data = new byte[comPort.bytesAvailable()]; // 创建缓冲区，大小为当前可用的字节数
                comPort.readBytes(data, data.length); // 读取数据
                if (serialPortConfig.getDataReceiver() != null) {
                    try {
                        serialPortConfig.getDataReceiver().receive(data);
                    }catch (Exception e){
                        log.error("串口："+serialPortConfig.getSerialPortName()+" 处理消息数据时发生异常",e);
                    }
                }
            }else if(serialPortEvent.getEventType() == SerialPort.LISTENING_EVENT_PORT_DISCONNECTED){
                log.info("串口：{} 断开连接！",serialPortConfig.getSerialPortName());
                comPort.closePort();
                reconnect();
            }
        }
    }

}
