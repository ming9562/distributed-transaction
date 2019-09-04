package com.yanmingchen.distributed.transaction.demo.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yanmingchen.distributed.transaction.core.annotation.CYMTransaction;
import com.yanmingchen.distributed.transaction.core.enums.TransactionTypeEnum;
import com.yanmingchen.distributed.transaction.demo.entity.order.TxOrder;
import com.yanmingchen.distributed.transaction.demo.order.mapper.TxOrderMapper;
import com.yanmingchen.distributed.transaction.demo.order.service.ITxOrderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author YanmingChen
 * @since 2019-08-06
 */
@Service
public class TxOrderServiceImpl extends ServiceImpl<TxOrderMapper, TxOrder> implements ITxOrderService {

    @Autowired
    private TxOrderMapper txOrderMapper;

//    @CYMTransaction(transactionType = TransactionTypeEnum.TCC, cancelMethod = "deleteLastInsertOrder")
    @CYMTransaction(transactionType = TransactionTypeEnum.TWO_PC, noRollbackFor = {ArithmeticException.class})
    @Override
    public void saveOrder(TxOrder txOrder) {
        System.out.println("saveOrder");
        txOrderMapper.insert(txOrder);
    }

    public void deleteLastInsertOrder(TxOrder txOrder) {
        System.out.println("deleteLastInsertOrder");
        txOrderMapper.deleteLastInsertOrder();
    }

}
