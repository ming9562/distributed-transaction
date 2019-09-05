package com.cym.distributed.transaction.core.netty.client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * @author: YanmingChen
 * @date: 2019-08-06
 * @time: 17:39
 * @description:
 */
public class NettyClientChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast(new NettyClientInHandler());//注册handler
    }

}
