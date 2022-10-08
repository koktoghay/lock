package com.kidult.practices.lock.mapper;

import com.kidult.practices.lock.domain.TGoods;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 商品表 Mapper 接口
 * </p>
 *
 * @author tommy
 * @since 2022-09-29
 */
public interface TGoodsMapper extends BaseMapper<TGoods> {

    TGoods selectForUpdate(String goodsId);

    int subGoodsStock(String goodsId);

    TGoods selectGoods(@Param("goodsId") String goodsId);

    int updateGoods(@Param("goodsId") String goodsId, @Param("version") int version);

}
