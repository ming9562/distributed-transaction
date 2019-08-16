package com.yanmingchen.distributed.transaction.demo.ui.client;

import com.yanmingchen.distributed.transaction.demo.entity.order.TxOrder;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author: YanmingChen
 * @date: 2019-08-06
 * @time: 11:41
 * @description:
 */
@FeignClient(value = "${order-server-name}")
public interface TxOrderClient {

    @PostMapping("/tx-order/saveOrder")
    void saveOrder(@RequestBody TxOrder txOrder);

}
