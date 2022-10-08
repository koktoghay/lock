package com.kidult.practices.lock.service;

import com.kidult.practices.lock.controller.RedisLockController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author Created by tommy on 2022/10/08.
 */
@Slf4j
@Service
public class RedisLockService {

    @Autowired
    private RedisTemplate redisTemplate;


    public boolean tryLock(String key, String value, int lockTimeout) {
        if (Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, value, lockTimeout, TimeUnit.SECONDS))) {
            return true;
        }
        return false;
    }

    /**
     * 模拟扣库存操作
     *
     * @param key
     * @param value
     * @param lockTimeout       锁的超时时长
     * @param stock
     * @param subStockSpendTime 扣库存花费时长
     *                          如果锁的超时时长小于扣库存时长，则锁会被再次获取（可以通过一个观察线程(watch dog)监视当前的线程是否已完成业务操作，如果没有则将锁的时长再次延长）
     * @param threadName
     * @return
     */
    public boolean subStock(String key, String value, int lockTimeout, int stock, int subStockSpendTime, String threadName) {
        if (tryLock(key, value, lockTimeout)) {
            try {
                long spendTime = subStockSpendTime * 1000L;
                while (spendTime > 0) {
                    log.info("threadName= " + threadName + ",正在扣除库存操作");
                    spendTime -= 1000;
                    Thread.sleep(1000);
                }
                if (stock > 0) {
                    stock = stock - 1;
                    log.info("threadName= " + threadName + ",缓存扣除成功,stock= " + stock);
                } else {
                    log.error("threadName= " + threadName + ",库存不足，无法操作");
                }
                return true;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

}
