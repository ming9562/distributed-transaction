package com.yanmingchen.distributed.transaction.core.netty.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

/**
 * @author: YanmingChen
 * @date: 2019-08-06
 * @time: 17:38
 * @description:
 */
public class NettyServerChannelInitializer extends ChannelInitializer<Channel> {

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline().addLast(new NettyServerInHandler());
        ch.pipeline().addLast(new NettyServerOutHandler());
    }

}
