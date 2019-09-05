package com.cym.distributed.transaction.demo.ui.client;

import com.cym.distributed.transaction.demo.entity.stock.TxStock;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author: YanmingChen
 * @date: 2019-08-06
 * @time: 11:41
 * @description:
 */
@FeignClient(value = "${stock-server-name}")
public interface TxStockClient {

    @PostMapping("/tx-stock/updateStock")
    void updateStock(@RequestBody TxStock txStock);

}
