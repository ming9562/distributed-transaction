package com.cym.distributed.transaction.demo.stock.controller;


import com.cym.distributed.transaction.demo.entity.stock.TxStock;
import com.cym.distributed.transaction.demo.stock.service.ITxStockService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author YanmingChen
 * @since 2019-08-06
 */
@RestController
@RequestMapping("/tx-stock")
public class TxStockController {

    @Autowired
    private ITxStockService txStockService;

    @PostMapping("/updateStock")
    public void updateStock(@RequestBody TxStock txStock) {
        txStockService.updateStock(txStock);
    }

}
