package com.kidult.practices.lock.service;

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

import java.sql.Connection;
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

    public int updateGoods(String id, int stock) {
        LambdaUpdateWrapper<TGoods> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TGoods::getId, id);
        TGoods goods = new TGoods();
        goods.setStock(stock);
        return goodsMapper.update(goods, updateWrapper);
    }


    public void addLock(TDistributedLock lock) {
        lockMapper.insert(lock);
    }

    public void deleteLock(String id) {
        lockMapper.deleteById(id);
    }

    public boolean subGoodsStock(String id) {
        boolean lock = false;
        try {
            Connection connection = jdbcTemplate.getDataSource().getConnection();

            log.info("before set autoCommit=" + connection.getAutoCommit());


            connection.setAutoCommit(true);
//            connection.commit();

            log.info("after set autoCommit=" + connection.getAutoCommit());

            StringBuilder sqlBuild = new StringBuilder();
            sqlBuild.append("select stock from t_goods where id='" + id + "' for update;\n");
            jdbcTemplate.execute(sqlBuild.toString());

            sqlBuild = new StringBuilder();
            sqlBuild.append("update t_goods set stock=stock-1 where id='" + id + "';\n");
            jdbcTemplate.execute(sqlBuild.toString());
            lock = true;
        } catch (Exception e) {
            e.printStackTrace();
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
