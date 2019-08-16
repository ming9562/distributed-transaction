package com.yanmingchen.distributed.transaction.core.netty.client;

import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * • 连接服务器 • 写数据到服务器 • 等待接受服务器返回相同的数据 • 关闭连接
 *
 */
public class NettyClient {

	private final String host;
	private final int port;

	public NettyClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public void start() throws Exception {
		EventLoopGroup nioEventLoopGroup = null;
		try {
			// 客户端引导类
			Bootstrap bootstrap = new Bootstrap();
			// EventLoopGroup可以理解为是一个线程池，这个线程池用来处理连接、接受数据、发送数据
			nioEventLoopGroup = new NioEventLoopGroup();
			bootstrap.group(nioEventLoopGroup)//多线程处理
					.channel(NioSocketChannel.class)//指定通道类型为NioServerSocketChannel，一种异步模式，OIO阻塞模式为OioServerSocketChannel
					.remoteAddress(new InetSocketAddress(host, port))//地址
					.handler(new NettyClientChannelInitializer());
			// 链接服务器
			ChannelFuture channelFuture = bootstrap.connect().sync();
			channelFuture.channel().closeFuture().sync();

		} finally {
				nioEventLoopGroup.shutdownGracefully();
		}
	}

	public static void main(String[] args) throws Exception {
		new NettyClient("localhost", 20000).start();
	}
}
