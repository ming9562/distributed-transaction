package com.cym.distributed.transaction.core.netty.server;

import com.cym.distributed.transaction.core.netty.utils.ByteBufToBytes;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author: YanmingChen
 * @date: 2019-08-06
 * @time: 16:54
 * @description:
 */
public class NettyServerInHandler extends ChannelInboundHandlerAdapter {

    /**
     * 读取信息调用
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        ByteBufToBytes byteBufToBytes = new ByteBufToBytes();
        byte[] read = byteBufToBytes.read(byteBuf);
        System.out.println(new String(read));
    }

    /**
     * 读取信息完成后调用
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    /**
     * 出异常调用
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
