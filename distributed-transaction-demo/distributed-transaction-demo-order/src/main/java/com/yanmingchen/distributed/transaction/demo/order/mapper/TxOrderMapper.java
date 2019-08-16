package com.yanmingchen.distributed.transaction.demo.order.mapper;

import com.yanmingchen.distributed.transaction.demo.entity.order.TxOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Delete;

/**
 * <p>
 * 订单表 Mapper 接口
 * </p>
 *
 * @author YanmingChen
 * @since 2019-08-06
 */
public interface TxOrderMapper extends BaseMapper<TxOrder> {

    @Delete("DELETE FROM tx_order WHERE id = (SELECT LAST_INSERT_ID())")
    void deleteLastInsertOrder();

}
