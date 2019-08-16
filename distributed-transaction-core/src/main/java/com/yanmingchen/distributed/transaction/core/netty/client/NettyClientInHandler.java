package com.yanmingchen.distributed.transaction.core.netty.client;

import java.util.Scanner;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author: YanmingChen
 * @date: 2019-08-06
 * @time: 16:55
 * @description:
 */
public class NettyClientInHandler extends SimpleChannelInboundHandler {

    /**
     * 连接到服务器后调用
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelActive...");
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String next = scanner.next();
            byte[] req = next.getBytes();//消息
            ByteBuf firstMessage = Unpooled.buffer(req.length);//创建一个空的ByteBuff用于缓存即将发送的数据
            firstMessage.writeBytes(req);//发送
            ctx.writeAndFlush(firstMessage);//flush
        }
    }

    /**
     * 从服务器读取到数据后调用
     * @param channelHandlerContext
     * @param o
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        System.out.println(o);
    }

    /**
     * 出异常时调用
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
