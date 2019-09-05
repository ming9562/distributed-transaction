package com.cym.distributed.transaction.demo.order.service;

import com.cym.distributed.transaction.demo.entity.order.TxOrder;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author YanmingChen
 * @since 2019-08-06
 */
public interface ITxOrderService extends IService<TxOrder> {

    void saveOrder(TxOrder txOrder);
}
