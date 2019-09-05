package com.cym.distributed.transaction.demo.order.controller;


import com.cym.distributed.transaction.demo.entity.order.TxOrder;
import com.cym.distributed.transaction.demo.order.service.ITxOrderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 订单表 前端控制器
 * </p>
 *
 * @author YanmingChen
 * @since 2019-08-06
 */
@RestController
@RequestMapping("/tx-order")
public class TxOrderController {

    @Autowired
    private ITxOrderService txOrderService;

    @PostMapping("/saveOrder")
    public void saveOrder(@RequestBody TxOrder txOrder) {
        txOrderService.saveOrder(txOrder);
    }

}
