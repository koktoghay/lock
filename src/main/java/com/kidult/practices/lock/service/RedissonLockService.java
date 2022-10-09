package com.kidult.practices.lock.service;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author Created by tommy on 2022/10/09.
 */
@Slf4j
@Service
public class RedissonLockService {

    @Autowired
    private RedissonClient redissonClient;

    public void setKv(String key, String value) {
        redissonClient.getBucket(key).set(value, 1, TimeUnit.MINUTES);
    }

    /**
     * 试图获取锁
     *
     * @param lockKey
     * @param waitTime  获取锁的等待时间
     * @param leaseTime 锁的任务执行时长
     * @param unit
     * @return
     */
    public boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            return lock.tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            return false;
        }
    }

    /**
     * 释放锁
     *
     * @param lockKey
     */
    public void unlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.isLocked()) {
            lock.unlock();
        }
    }

    /**
     * 获取锁，执行任务操作
     *
     * @param invoker
     * @param waitTime
     * @param leaseTime
     */
    public void lockWithInvoke(Invoker invoker, long waitTime, long leaseTime) {
        if (tryLock(invoker.getLockKey(), waitTime, leaseTime, TimeUnit.SECONDS)) {
            try {
                invoker.doInvoke();
            } finally {
                unlock(invoker.getLockKey());
            }
        }
    }

}
