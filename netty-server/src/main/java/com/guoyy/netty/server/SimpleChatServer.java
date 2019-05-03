package com.guoyy.netty.server;

import com.guoyy.netty.conf.HostInfo;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.net.InetSocketAddress;

public class SimpleChatServer {

    public static void main(String[] args) {

        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap server = new ServerBootstrap();

            server.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new SimpleChatServerInitializer())
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .option(ChannelOption.SO_REUSEADDR, true);

            ChannelFuture channelFuture = server.bind(new InetSocketAddress(HostInfo.rpcSrvIp, HostInfo.rpcSrvPort))
                    .addListener((future) -> {
                        if (future.isSuccess()) {
                            System.out.println(String.format("服务器启动[%s:%s]", HostInfo.rpcSrvIp, HostInfo.rpcSrvPort));
                        }
                    }).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static class SimpleChatServerInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
            ChannelPipeline pipeline = socketChannel.pipeline();
            pipeline.addLast(new LengthFieldBasedFrameDecoder(65535, 0, 2, 0, 2));
            pipeline.addLast(new LengthFieldPrepender(2));
            pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));
            pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8));
            pipeline.addLast(new SimpleChatServerHandler());

            System.out.println("SimpleChatClient:" + socketChannel.remoteAddress() + " 连接上...");
        }
    }

    public static class SimpleChatServerHandler extends SimpleChannelInboundHandler<String> {
        public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
            Channel incoming = ctx.channel();
            for (Channel channel : channels) {
                channel.writeAndFlush("[Server]-" + incoming.remoteAddress() + " 加入.");
            }
            channels.add(incoming);
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
            Channel incoming = ctx.channel();
            for (Channel channel : channels) {
                channel.writeAndFlush("[Server]-" + incoming.remoteAddress() + " 离开.");
            }
            channels.remove(incoming);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            Channel channel = ctx.channel();
            System.out.println("SimpleChatClient:" + channel.remoteAddress() + " 在线.");
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            Channel channel = ctx.channel();
            System.out.println("SimpleChatClient:" + channel.remoteAddress() + " 掉线.");
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            System.out.println("SimpleChatClient:" + ctx.channel().remoteAddress() + " 异常.");
            cause.printStackTrace();
            ctx.close();
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
            Channel incoming = ctx.channel();
            String prefix = "[you]";
            for (Channel channel : channels) {
                if (channel != incoming) {
                    prefix = String.format("[%s]", incoming.remoteAddress());
                }
                channel.writeAndFlush(String.format("%s %s.", prefix, s));
            }
            if ("exit".equalsIgnoreCase(s)) {
                ctx.close();
            }
        }
    }
}
