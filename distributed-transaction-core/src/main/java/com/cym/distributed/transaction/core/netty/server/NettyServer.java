package com.cym.distributed.transaction.core.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * • 配置服务器功能，如线程、端口 • 实现服务器处理程序，它包含业务逻辑，决定当有一个请求连接或接收数据时该做什么
 * 
 * @author wilson
 * 
 */
public class NettyServer {

	private final int port;

	public NettyServer(int port) {
		this.port = port;
	}

	public void start() throws Exception {
		EventLoopGroup eventLoopGroup = null;
		try {
			// server端引导类
			ServerBootstrap serverBootstrap = new ServerBootstrap();
			// 连接池处理数据
			eventLoopGroup = new NioEventLoopGroup();
			serverBootstrap.group(eventLoopGroup).channel(NioServerSocketChannel.class)// 指定通道类型为NioServerSocketChannel，一种异步模式，OIO阻塞模式为OioServerSocketChannel
					.localAddress("localhost", port)// 设置InetSocketAddress让服务器监听某个端口已等待客户端连接。
					.childHandler(new NettyServerChannelInitializer());
			// 最后绑定服务器等待直到绑定完成，调用sync()方法会阻塞直到服务器完成绑定
			ChannelFuture channelFuture = serverBootstrap.bind().sync();
			System.out.println("开始监听，端口为：" + channelFuture.channel().localAddress());
			// 等待channel关闭，因为使用sync()，所以关闭操作也会被阻塞。
			channelFuture.channel().closeFuture().sync();
		} finally {
			// 阻塞等待线程组关闭
			eventLoopGroup.shutdownGracefully().sync();
		}
	}

	public static void main(String[] args) throws Exception {
		new NettyServer(20000).start();
	}
}
