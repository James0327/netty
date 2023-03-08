package com.guoyy.netty.server;

import com.alibaba.fastjson2.JSON;
import com.guoyy.netty.conf.HostInfo;
import com.guoyy.netty.dto.UserInfo;
import com.guoyy.netty.util.MsgpackSerialize;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.util.ReferenceCountUtil;

import java.util.Date;

/**
 * Description: netty
 * com.guoyy.netty.server.NettyHttpServer
 *
 * @author guoyiyong/james
 * Date: 2022/11/1 21:05
 * Version: 1.0
 *
 * Copyright (C) 2022 JW All rights reserved.
 */
public class NettyHttpServer {

    public static void main(String[] args) {

        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            ChannelFuture channelFuture = bootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .option(ChannelOption.SO_BACKLOG, 8192)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(65535, 0, 2, 0, 2));
                            pipeline.addLast(new LengthFieldPrepender(2));
                            pipeline.addLast(new MsgpackSerialize.Encoder());
                            pipeline.addLast(new MsgpackSerialize.Decoder());
                            pipeline.addLast(new WriteTimeoutHandler(120));
                            pipeline.addLast(new NettyHttpServerHandler());
                        }
                    })
                    .childOption(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.SO_BACKLOG, 8192)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .bind(HostInfo.rpcSrvIp, HostInfo.rpcSrvPort)
                    .addListener((ChannelFutureListener)future -> {
                        if (future.isSuccess()) {
                            System.out.printf("服务器地址[%s]，监听端口[%d]已启动，可以正常通讯。%n", HostInfo.rpcSrvIp, HostInfo.rpcSrvPort);
                        }
                    }).sync();

            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            workGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }

    }

    private static class NettyHttpServerHandler extends ChannelInboundHandlerAdapter {
        /**
         * 2
         *
         * @param ctx
         * @throws Exception
         */
        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            super.channelRegistered(ctx);

            System.out.println("channelRegistered " + ctx);
        }

        /**
         * 3
         *
         * @param ctx
         * @throws Exception
         */
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);

            System.out.println("channelActive " + ctx);
        }

        /**
         * 4
         *
         * @param ctx
         * @param msg
         * @throws Exception
         */
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            try {
                System.out.println("server channelRead " + ctx);

                UserInfo userInfo = JSON.parseObject((String)msg, UserInfo.class);
                userInfo.setBirthday(new Date());
                userInfo.setAge(25);

                ctx.channel().writeAndFlush(userInfo);
            } finally {
                ReferenceCountUtil.release(msg);
            }
        }

        /**
         * 5
         *
         * @param ctx
         * @throws Exception
         */
        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            super.channelReadComplete(ctx);
        }

        /**
         * 1
         *
         * @param ctx
         * @throws Exception
         */
        @Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
            super.handlerAdded(ctx);
            System.out.println("handlerAdded " + ctx);
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
            super.handlerRemoved(ctx);
            System.out.println("handlerRemoved " + ctx);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }

}
