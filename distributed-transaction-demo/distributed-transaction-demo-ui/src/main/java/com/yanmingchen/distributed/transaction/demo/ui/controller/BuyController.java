package com.yanmingchen.distributed.transaction.demo.ui.controller;

import com.yanmingchen.distributed.transaction.demo.ui.service.BuyService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author: YanmingChen
 * @date: 2019-08-06
 * @time: 11:39
 * @description:
 */
@Api(tags = "购物API")
@RestController
@RequestMapping(value = "/buy")
public class BuyController {

    @Autowired
    private BuyService buyService;

    @ApiOperation("购买商品")
    @PostMapping("/buyGoods")
    public void buyGoods() {
        buyService.buyGoods();
    }

}
