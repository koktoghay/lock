package com.kidult.practices.lock.service;

import lombok.extern.slf4j.Slf4j;
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
}
