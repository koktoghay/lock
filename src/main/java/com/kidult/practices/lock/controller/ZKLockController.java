package com.kidult.practices.lock.controller;

import com.kidult.practices.lock.service.Invoker;
import com.kidult.practices.lock.service.ZKLockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Created by tommy on 2022/10/12.
 */
@Slf4j
@RestController
@RequestMapping("/zkLock")
public class ZKLockController {

    static int concurrencyThreadCount = 5;

    static int stock = 3;

    @Autowired
    private ZKLockService zkLockService;

    /**
     * 模拟并发扣库存操作
     *
     * @param key
     * @param value
     * @param lockTimeout
     * @return
     */
    @RequestMapping("/tryLock")
    public Object tryLock(String key, String value, int lockTimeout) {
        CountDownLatch downLatch = new CountDownLatch(concurrencyThreadCount);
        for (int i = 0; i < concurrencyThreadCount; i++) {
            String threadName = "subThread-" + (i + 1);
            ZKLockController.SubStockRunnable subStockRunnable = new ZKLockController.SubStockRunnable(downLatch, key, value, lockTimeout, threadName);
            Thread subThread = new Thread(subStockRunnable);
            subThread.setName(threadName);
            subThread.start();
            downLatch.countDown();
        }
        return 1;
    }

    class SubStockRunnable implements Runnable {

        private CountDownLatch downLatch;

        private String key;

        private String value;

        private int lockTimeout;

        private String threadName;

        SubStockRunnable(CountDownLatch downLatch, String key, String value, int lockTimeout, String threadName) {
            this.downLatch = downLatch;
            this.key = key;
            this.value = value;
            this.lockTimeout = lockTimeout;
            this.threadName = threadName;
        }

        @Override
        public void run() {
            try {
                log.info("threadName= {}, 进入等待队列", threadName);
                downLatch.await();

                zkLockService.lockWithInvoke(key, new Invoker() {
                    @Override
                    public void doInvoke() {
                        try {
                            Thread.sleep(5000);
                        } catch (Exception e) {
                        }
                    }

                    @Override
                    public String getLockKey() {
                        return key;
                    }
                }, lockTimeout, TimeUnit.SECONDS, threadName);
            } catch (Exception e) {
                log.error("subGoodsStock ex:", e);
            }
        }
    }
}
