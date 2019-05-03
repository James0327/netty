package com.guoyy.netty.client;

import com.alibaba.fastjson.JSON;
import com.guoyy.netty.conf.HostInfo;
import com.guoyy.netty.dto.UserConf;
import com.guoyy.netty.dto.UserInfo;
import com.guoyy.netty.util.MsgpackSerialize;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.util.ReferenceCountUtil;

import java.util.Scanner;

/**
 * com.guoyy.netty.http.client
 *
 * @description:
 * @author: guoyiyong
 * @date: 2019/2/16
 * @time: 19:41
 * @@ Copyright (C) 2018 MTDP All rights reserved
 */
public class TcpClient {
    public static void main(String[] args) {

        NioEventLoopGroup clienGroup = new NioEventLoopGroup();

        try {
            Bootstrap client = new Bootstrap();
            client.group(clienGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();

                            pipeline.addLast(new LengthFieldBasedFrameDecoder(65535, 0, 2, 0, 2));
                            pipeline.addLast(new LengthFieldPrepender(2));
                            pipeline.addLast(new MsgpackSerialize.Encoder());
                            pipeline.addLast(new MsgpackSerialize.Decoder());
                            pipeline.addLast(new TcpClientHandler());
                        }
                    }).option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture channelFuture = client
                    .connect(HostInfo.rpcSrvIp, HostInfo.rpcSrvPort)
                    .addListener(future -> {
                        if (future.isSuccess()) {
                            System.out.println(String.format("已和服务器[%s:%d]建立连接，可以正常进行业务处理。", HostInfo.rpcSrvIp, HostInfo.rpcSrvPort));
                        }
                    }).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            clienGroup.shutdownGracefully();
        }
    }

    private static class TcpClientHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) {
//            try {
//                Object obj = MessageQueue.take();
//                System.out.println("obj:" + obj);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

//            new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            scanner.useDelimiter("\n");
            while (scanner.hasNext()) {
                try {
                    int type = scanner.nextInt();
                    String line = scanner.next();
                    if (type == 1) {
                        UserInfo userInfo = JSON.parseObject(line, UserInfo.class);
                        ctx.writeAndFlush(userInfo);
                    } else if (type == 2) {
                        UserConf userConf = JSON.parseObject(line, UserConf.class);
                        ctx.writeAndFlush(userConf);
                    } else {
                        ctx.writeAndFlush(line);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
//            }).start();
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            try {
                System.out.println(msg);
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
