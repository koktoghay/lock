package com.kidult.practices.lock.controller;

import com.kidult.practices.lock.service.RedissonLockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Created by tommy on 2022/10/09.
 */
@Slf4j
@RestController
@RequestMapping("/redissonLock")
public class RedissonLockController {

    @Autowired
    private RedissonLockService redissonLockService;

    @RequestMapping("/setKv")
    public Object setKv(String key, String value) {
        redissonLockService.setKv(key, value);
        return 1;
    }

}
