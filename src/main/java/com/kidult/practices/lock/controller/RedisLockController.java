package com.kidult.practices.lock.controller;

import com.kidult.practices.lock.service.RedisLockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CountDownLatch;

/**
 * @author Created by tommy on 2022/10/08.
 */
@Slf4j
@RestController
@RequestMapping("/redisLock")
public class RedisLockController {

    static int concurrencyThreadCount = 5;

    static int stock = 3;

    @Autowired
    private RedisLockService redisLockService;

    /**
     * 模拟并发扣库存操作
     *
     * @param key
     * @param value
     * @param lockTimeout
     * @return
     */
    // http://localhost:9088/redisLock/tryLock?key=mylock&value=1&lockTimeout=3
    @RequestMapping("/tryLock")
    public Object tryLock(String key, String value, int lockTimeout) {
        CountDownLatch downLatch = new CountDownLatch(concurrencyThreadCount);
        for (int i = 0; i < concurrencyThreadCount; i++) {
            String threadName = "subThread-" + (i + 1);
            SubStockRunnable subStockRunnable = new SubStockRunnable(downLatch, key, value, lockTimeout, threadName);
            Thread subThread = new Thread(subStockRunnable);
            subThread.setName(threadName);
            subThread.start();
            downLatch.countDown();

//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        downLatch.await();
//                        boolean handlerFlag = redisLockService.tryLock(key, value, lockTimeout);
//                        log.info("handlerFlag:{}", handlerFlag);
//                    } catch (Exception e) {
//                        log.error("subGoodsStock ex:", e);
//                    }
//                }
//            }).start();
//            downLatch.countDown();
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
                boolean handlerFlag = redisLockService.subStock(key, value, lockTimeout, stock, 15, threadName);
                log.info("threadName= {}, handlerFlag={}", threadName, handlerFlag);
            } catch (Exception e) {
                log.error("subGoodsStock ex:", e);
            }
        }
    }
}
