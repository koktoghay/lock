package com.kidult.practices.lock.service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.kidult.practices.lock.domain.TDistributedLock;
import com.kidult.practices.lock.domain.TGoods;
import com.kidult.practices.lock.mapper.TDistributedLockMapper;
import com.kidult.practices.lock.mapper.TGoodsMapper;
import com.kidult.practices.lock.utils.UniqueID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author Created by tommy on 2022/09/29.
 */
@Slf4j
@Service
public class DBLockService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TDistributedLockMapper lockMapper;

    @Autowired
    private TGoodsMapper goodsMapper;

    public boolean addLock(String businessKey, String businessDesc) {
        boolean lock;
        String lockId = UniqueID.INSTANCE.nextId();
        try {
            TDistributedLock distributedLock = new TDistributedLock();
            distributedLock.setId(lockId);
            distributedLock.setBusinessKey(businessKey);
            distributedLock.setBusinessDesc(businessDesc);
            lockMapper.insert(distributedLock);
            lock = true;
        } catch (Exception e) {
            lock = false;
            log.error("获取锁异常,e:", e);
        } finally {
            deleteLock(lockId);
        }
        return lock;
    }

    public void deleteLock(String id) {
        lockMapper.deleteById(id);
    }


    public int updateGoods(String id, int stock) {
        LambdaUpdateWrapper<TGoods> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TGoods::getId, id);
        TGoods goods = new TGoods();
        goods.setStock(stock);
        return goodsMapper.update(goods, updateWrapper);
    }

    @Transactional(rollbackFor = Exception.class, timeout = 50)
    public boolean subGoodsStock(String id) {
        boolean lock;
        try {
            TGoods goods = goodsMapper.selectForUpdate(id);
            log.info("subGoodsStock goods:{}", JSON.toJSONString(goods));
            if (goods.getStock() > 0) {
                int updateFlag = goodsMapper.subGoodsStock(goods.getId());
                log.info("subGoodsStock updateFlag:{}", updateFlag);
            }
            lock = true;
        } catch (Exception e) {
            log.error("获取锁异常,e:", e);
            lock = false;
        }
        return lock;
    }

    public List<TDistributedLock> queryLockList() {
        String sql = "select * from t_distributed_lock";
        return jdbcTemplate.query(sql, new RowMapper<TDistributedLock>() {
            @Override
            public TDistributedLock mapRow(ResultSet rs, int rowNum) throws SQLException {
                TDistributedLock s = new TDistributedLock();

                s.setId(rs.getString("id"));
                s.setBusinessKey(rs.getString("business_key"));
                s.setBusinessDesc(rs.getString("business_desc"));
                s.setCreateTime(LocalDateTime.parse(rs.getString("create_time"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                return s;
            }
        });
    }

}
