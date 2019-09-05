package com.cym.distributed.transaction.demo.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cym.distributed.transaction.core.annotation.CYMTransactional;
import com.cym.distributed.transaction.core.enums.TransactionTypeEnum;
import com.cym.distributed.transaction.demo.entity.order.TxOrder;
import com.cym.distributed.transaction.demo.order.mapper.TxOrderMapper;
import com.cym.distributed.transaction.demo.order.service.ITxOrderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

//    @CYMTransactional(transactionType = TransactionTypeEnum.TCC, cancelMethod = "deleteLastInsertOrder", noRollbackFor = {ArithmeticException.class})
    @CYMTransactional(transactionType = TransactionTypeEnum.TWO_PC, noRollbackFor = {ArithmeticException.class})
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
