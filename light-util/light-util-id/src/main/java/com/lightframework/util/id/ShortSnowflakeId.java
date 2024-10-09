package com.lightframework.util.id;

import cn.hutool.core.util.IdUtil;

public class ShortSnowflakeId {

    private static ShortSnowflakeId snowflakeIdWorker = new ShortSnowflakeId();

    // 时间戳位数
    private static final int TIMESTAMP_BITS = 16;
    // 数据中心ID位数
    private static final int DATACENTER_ID_BITS = 2;
    // 机器ID位数
    private static final int WORKER_ID_BITS = 2;
    // 序列号位数
    private static final int SEQUENCE_BITS = 12;
    // 最大支持的数据中心ID
    private static final int MAX_DATACENTER_ID = ~(-1 << DATACENTER_ID_BITS);
    // 最大支持的机器ID
    private static final int MAX_WORKER_ID = ~(-1 << WORKER_ID_BITS);

    // 序列号掩码
    private static final int SEQUENCE_MASK = ~(-1 << SEQUENCE_BITS);
    // 时间戳左移位数
    private static final int TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS;
    // 数据中心ID左移位数
    private static final int DATACENTER_ID_SHIFT = TIMESTAMP_LEFT_SHIFT + TIMESTAMP_BITS;
    // 机器ID左移位数
    private static final int WORKER_ID_SHIFT = DATACENTER_ID_SHIFT + DATACENTER_ID_BITS;

    // 开始时间戳（可自定义，但应确保是一个过去的时间点）
    private static final long TWEPOCH = 1288834974657L;

    private int datacenterId;
    private int workerId;
    private int sequence = 0;
    private long lastTimestamp = -1L;

    public ShortSnowflakeId(int datacenterId, int workerId) {
        if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
            throw new IllegalArgumentException("Datacenter ID can't be greater than " + MAX_DATACENTER_ID + " or less than 0");
        }
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException("Worker ID can't be greater than " + MAX_WORKER_ID + " or less than 0");
        }
        this.datacenterId = datacenterId;
        this.workerId = workerId;
    }

    public synchronized int nextId() {
        long timestamp = timeGen();
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) { // 如果序列号溢出，等待下一个毫秒
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else { // 如果时间戳改变，重置序列号
            sequence = 0;
        }
        lastTimestamp = timestamp;
        return ((int) (timestamp - TWEPOCH) << TIMESTAMP_LEFT_SHIFT) | (datacenterId << DATACENTER_ID_SHIFT) | (workerId << WORKER_ID_SHIFT) | sequence;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) { // 如果当前时间小于或等于上一次生成ID的时间戳，就一直等待下一个毫秒
            timestamp = timeGen();
        }
        return timestamp;
    }

    private ShortSnowflakeId() {
        this((int) IdUtil.getWorkerId(IdUtil.getDataCenterId(MAX_DATACENTER_ID), MAX_WORKER_ID),(int)IdUtil.getDataCenterId(MAX_DATACENTER_ID));
    }

    public static int getNextId(){
        return snowflakeIdWorker.nextId();
    }

}