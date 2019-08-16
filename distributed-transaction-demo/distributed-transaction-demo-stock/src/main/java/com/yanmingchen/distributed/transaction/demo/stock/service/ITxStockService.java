package com.yanmingchen.distributed.transaction.demo.stock.service;

import com.yanmingchen.distributed.transaction.demo.entity.stock.TxStock;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author YanmingChen
 * @since 2019-08-06
 */
public interface ITxStockService extends IService<TxStock> {

    void updateStock(TxStock txStock);
}
