package com.kidult.practices.lock.utils;


import org.apache.commons.lang3.time.DateFormatUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author: zhangliang
 * @Description: snowflake生成ID
 * @Date: 18/8/3
 */
public enum UniqueID {
    INSTANCE;

    /**
     * 用 IP 地址最后几个字节标识
     */
    private long workerId;

    private long dataCenterId = 0L;

    private long sequence = 0L;

    private long workerIdBits = 5L;//节点ID长度

    private long sequenceBits = 12L;//序列号12位

    private long workerIdShift = sequenceBits;//机器节点左移12位

    private long dataCenterIdShift = sequenceBits + workerIdBits; //数据中心节点左移17位

    private long sequenceMask = -1L ^ (-1 << sequenceBits); //2的12次方-1

    private long lastTimestamp = -1L;


    UniqueID() {
        workerId = 0x000000FF & getLastIP();
    }

    public synchronized String nextId(String prefix) {
        long timestamp = time();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format("Clock is not right. milliseconds: %d", lastTimestamp - timestamp));
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                timestamp = nextMillisTime(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        long suffix = (workerId << workerIdShift) | (dataCenterId << dataCenterIdShift) | sequence;
        String datePrefix = DateFormatUtils.format(timestamp, "yyyyMMddHHmmssSSS");
        return prefix + suffix + datePrefix;
    }

    public synchronized String nextId() {
        return nextId("");
    }


    private byte getLastIP() {
        byte lastIP = 0;
        try {
            InetAddress ip = InetAddress.getLocalHost();
            byte[] ipByte = ip.getAddress();
            lastIP = ipByte[ipByte.length - 1];
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return lastIP;
    }

    protected long nextMillisTime(long lastTimestamp) {
        long timestamp = time();
        while (timestamp <= lastTimestamp) {
            timestamp = time();
        }
        return timestamp;
    }

    protected long time() {
        return System.currentTimeMillis();
    }
}
