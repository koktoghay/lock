package com.kidult.practices.lock.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author Created by tommy on 2022/10/12.
 */
@Slf4j
@Service
public class ZKLockService {

    private static final String LOCK_PATH = "/zklock";

    @Autowired
    private CuratorFramework curatorFramework;

    public void lockWithInvoke(String lockKey, Invoker invoker, long leaseTime, TimeUnit unit, String threadName) {
        InterProcessMutex lock = new InterProcessMutex(curatorFramework, LOCK_PATH + "/" + lockKey);
        try {
            if (lock.acquire(leaseTime, unit)) {
                log.info("线程" + threadName + "已获取到锁");
                invoker.doInvoke();
                log.info("线程" + threadName + "处理成功");
            }
        } catch (Exception e) {
            log.error("加锁异常,e:", e);
        } finally {
            try {
                if (lock.isOwnedByCurrentThread()) {
                    lock.release();
                }
            } catch (Exception ex) {
                log.error("解锁异常,ex:", ex);
            }
        }
    }

}
