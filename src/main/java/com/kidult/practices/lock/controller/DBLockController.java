package com.kidult.practices.lock.controller;

import com.alibaba.fastjson2.JSON;
import com.kidult.practices.lock.domain.TDistributedLock;
import com.kidult.practices.lock.service.DBLockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author Created by tommy on 2022/09/29.
 */
@Slf4j
@RestController
@RequestMapping("/dbLock")
public class DBLockController implements Serializable {

    static int concurrencyThreadCount = 5;

    @Autowired
    private DBLockService dbLockService;

    @RequestMapping("/updateGoodsStock")
    public Object updateGoodsStock(String goodsId, int stock) {
        return dbLockService.updateGoods(goodsId, stock);
    }

    @GetMapping("/subGoodsStock")
    public Object subGoodsStock(String goodsId) {
        CountDownLatch downLatch = new CountDownLatch(concurrencyThreadCount);
        for (int i = 0; i < concurrencyThreadCount; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        downLatch.await();
                        boolean handlerFlag = dbLockService.subGoodsStock(goodsId);
                        log.info("handlerFlag:{}", handlerFlag);
                    } catch (Exception e) {
                        log.error("subGoodsStock ex:", e);
                    }
                }
            }).start();
            downLatch.countDown();
        }
        return 1;
    }

    @GetMapping("/queryLockList")
    public Object queryLockList() {
        List<TDistributedLock> lockList = dbLockService.queryLockList();
        return JSON.toJSONString(lockList);
    }

}
