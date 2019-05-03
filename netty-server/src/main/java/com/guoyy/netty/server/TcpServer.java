package com.guoyy.netty.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.guoyy.netty.conf.HostInfo;
import com.guoyy.netty.util.MsgpackSerialize;
import com.guoyy.netty.dto.Pair;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.util.ReferenceCountUtil;

import java.net.InetSocketAddress;

/**
 * com.guoyy.netty.http.server
 *
 * @description:
 * @author: guoyiyong
 * @date: 2019/2/16
 * @time: 19:41
 * @@ Copyright (C) 2018 MTDP All rights reserved
 */
public class TcpServer {
    public static void main(String[] args) {

        NioEventLoopGroup boosGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap server = new ServerBootstrap();
            server.group(boosGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();

                            pipeline.addLast(new LengthFieldBasedFrameDecoder(65535, 0, 2, 0, 2));
                            pipeline.addLast(new LengthFieldPrepender(2));
                            pipeline.addLast(new MsgpackSerialize.Encoder());
                            pipeline.addLast(new MsgpackSerialize.Decoder());
                            pipeline.addLast(new WriteTimeoutHandler(120));
                            pipeline.addLast(new TcpServerHandler());
                        }
                    }).option(ChannelOption.SO_BACKLOG, 8192)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture channelFuture = server.bind(new InetSocketAddress(HostInfo.rpcSrvPort)).addListener(future -> {
                if (future.isSuccess()) {
                    System.out.println(String.format("服务器地址[%s]，监听端口[%d]已启动，可以正常通讯。", HostInfo.rpcSrvIp, HostInfo.rpcSrvPort));
                }
            }).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workGroup.shutdownGracefully();
            boosGroup.shutdownGracefully();
        }
    }

    private static class TcpServerHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            try {
                System.out.println("TcpServerHandler channelRead msg: " + msg);

                if (msg instanceof Pair) {
                    Pair pair = (Pair) msg;
                    JSONObject jsonObj = JSON.parseObject(pair.getJsonStr());
                    jsonObj.put("RpcServerTime", System.currentTimeMillis());
                    pair.setJsonStr(jsonObj.toJSONString());

                    ctx.write(pair);
                } else {
                    String jsonStr = (String) msg;
                    JSONObject jsonObj = JSON.parseObject(jsonStr);
                    jsonObj.put("RpcServerTime", System.currentTimeMillis());

                    ctx.write(jsonObj.toJSONString());
                }
            } finally {
                ReferenceCountUtil.release(msg);
            }
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
