package com.kidult.practices.lock.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * @author Created by tommy on 2022/10/08.
 */
@Slf4j
@Service
public class RedisLockService {

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 尝试加锁
     *
     * @param key
     * @param value       存放当前处理线程的唯一id（后续解锁时需要使用，即加锁解锁需要同一个线程操作）
     * @param lockTimeout
     * @return
     */
    public boolean tryLock(String key, String value, int lockTimeout) {
        if (Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, value, lockTimeout, TimeUnit.SECONDS))) {
            return true;
        }
        return false;
    }

    /**
     * 尝试加锁（支持等待超时，满足非阻塞特性）
     *
     * @param key
     * @param value
     * @param lockTimeout
     * @param waitTimeout
     * @return
     */
    public boolean tryLock(String key, String value, int lockTimeout, int waitTimeout) {
        boolean result = tryLock(key, value, lockTimeout);
        while ((!result) && waitTimeout-- > 0) {
            waitTimeout -= 1000;
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                log.error("加锁等待异常,e:", e);
            }
            result = tryLock(key, value, lockTimeout);
        }
        return result;
    }

    /**
     * @param key
     * @param value 存放当前处理线程的唯一id（后续解锁时需要使用，即加锁解锁需要同一个线程操作）
     * @return
     */
    public boolean releaseLock(String key, String value) {
        // 因为get和del操作并不是原子的，所以使用lua脚本
        String luaScripts = "if redis.call('get',KEYS[1]) == ARGV[1] then  return redis.call('del',KEYS[1]) else return 0  end;";

        // 执行 lua 脚本
//        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
//        // 指定 lua 脚本
//        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("redis/DelKey.lua")));
//        // 指定返回类型
//        redisScript.setResultType(Long.class);
//        // 参数一：redisScript，参数二：key列表，参数三：arg（可多个）
//        Long result = redisTemplate.execute(redisScript, Collections.singletonList(lockKey),UUID);

        // 指定 lua 脚本，并且指定返回值类型
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(luaScripts, Long.class);
        // 参数一：redisScript，参数二：key列表，参数三：arg（可多个）
        Long result = (Long) redisTemplate.execute(redisScript, Collections.singletonList(key), Collections.singletonList(value));
        return 1L == result;
    }

    /**
     * 模拟扣库存操作
     *
     * @param key
     * @param value             存放当前处理线程的唯一id（后续解锁时需要使用，即加锁解锁需要同一个线程操作）
     * @param lockTimeout       锁的超时时长
     * @param stock
     * @param subStockSpendTime 扣库存花费时长
     *                          如果锁的超时时长小于扣库存时长，则锁会被再次获取（可以通过一个观察线程(watch dog)监视当前的线程是否已完成业务操作，如果没有则将锁的超时时长再次延长）
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
            } catch (Exception e) {
                log.error("扣库存异常,e:", e);
            }
        }
        return false;
    }

}
