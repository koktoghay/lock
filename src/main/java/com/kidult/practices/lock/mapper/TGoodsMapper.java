package com.kidult.practices.lock.mapper;

import com.kidult.practices.lock.domain.TGoods;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
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

    TGoods selectForUpdate(String id);

    int subGoodsStock(String goodsId);

}
