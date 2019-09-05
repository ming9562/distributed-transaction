package com.cym.distributed.transaction.demo.ui.service.impl;

import com.cym.distributed.transaction.core.annotation.CYMTransactional;
import com.cym.distributed.transaction.core.enums.TransactionTypeEnum;
import com.cym.distributed.transaction.demo.entity.order.TxOrder;
import com.cym.distributed.transaction.demo.entity.stock.TxStock;
import com.cym.distributed.transaction.demo.ui.client.TxOrderClient;
import com.cym.distributed.transaction.demo.ui.client.TxStockClient;
import com.cym.distributed.transaction.demo.ui.service.BuyService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

/**
 * @author: YanmingChen
 * @date: 2019-08-06
 * @time: 11:41
 * @description:
 */
@Service
public class BuyServiceImpl implements BuyService {

    @Autowired
    private TxOrderClient txOrderClient;

    @Autowired
    private TxStockClient txStockClient;

//    @CYMTransactional(transactionType = TransactionTypeEnum.TCC, noRollbackFor = {ArithmeticException.class})
    @CYMTransactional(transactionType = TransactionTypeEnum.TWO_PC, noRollbackFor = {ArithmeticException.class})
    @Override
    public void buyGoods() {
        System.out.println("buyGoods");
        TxOrder txOrder = new TxOrder();
        txOrder.setBuyerName("张三");
        txOrder.setBuyerName("18824125129");
        txOrder.setGoodsId(1);
        txOrder.setGoodsPrice(new BigDecimal("3988"));
        txOrder.setBuyCount(1);
        txOrder.setCode(UUID.randomUUID().toString());
        txOrder.setCreateTime(new Date());
        txOrderClient.saveOrder(txOrder);

        TxStock txStock = new TxStock();
        txStock.setGoodsId(1);
        txStock.setSelledCount(1);
        txStock.setRemainCount(-1);
        txStockClient.updateStock(txStock);
    }

}
