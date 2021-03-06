package com.cym.distributed.transaction.demo.stock.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cym.distributed.transaction.core.annotation.CYMTransactional;
import com.cym.distributed.transaction.core.enums.TransactionTypeEnum;
import com.cym.distributed.transaction.demo.entity.stock.TxStock;
import com.cym.distributed.transaction.demo.stock.mapper.TxStockMapper;
import com.cym.distributed.transaction.demo.stock.service.ITxStockService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author YanmingChen
 * @since 2019-08-06
 */
@Service
public class TxStockServiceImpl extends ServiceImpl<TxStockMapper, TxStock> implements ITxStockService {

    @Autowired
    private TxStockMapper txStockMapper;

//    @CYMTransactional(transactionType = TransactionTypeEnum.TCC, cancelMethod = "addStock", noRollbackFor = {ArithmeticException.class})
    @CYMTransactional(transactionType = TransactionTypeEnum.TWO_PC, noRollbackFor = {ArithmeticException.class})
    @Override
    public void updateStock(TxStock txStock) {
        System.out.println("updateStock");
        QueryWrapper<TxStock> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TxStock::getGoodsId, txStock.getGoodsId());
        TxStock stock = txStockMapper.selectOne(queryWrapper);
        stock.setSelledCount(stock.getSelledCount() + txStock.getSelledCount());
        stock.setRemainCount(stock.getRemainCount() + txStock.getRemainCount());
        txStockMapper.updateById(stock);
//        int i = 1 / 0; // java.lang.ArithmeticException
//        Boolean b = null;
//        boolean bb = b == false; // java.lang.NullPointerException
    }

    public void addStock(TxStock txStock) {
        System.out.println("addStock");
        QueryWrapper<TxStock> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TxStock::getGoodsId, txStock.getGoodsId());
        TxStock stock = txStockMapper.selectOne(queryWrapper);
        stock.setSelledCount(stock.getSelledCount() - txStock.getSelledCount());
        stock.setRemainCount(stock.getRemainCount() - txStock.getRemainCount());
        txStockMapper.updateById(stock);
    }

}
