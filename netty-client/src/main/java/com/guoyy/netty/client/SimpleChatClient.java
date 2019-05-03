package com.guoyy.netty.client;

import com.guoyy.netty.conf.HostInfo;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.util.Scanner;

public class SimpleChatClient {

    public static void main(String[] args) {

        NioEventLoopGroup clientGroup = new NioEventLoopGroup();
        try {
            Bootstrap client = new Bootstrap();
            client.group(clientGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new SimpleChatClientInitializer())
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture channelFuture = client.connect(HostInfo.rpcSrvIp, HostInfo.rpcSrvPort).addListener((future -> {
                if (future.isSuccess()) {
                    System.out.println(String.format("连接服务器[%s:%s]", HostInfo.rpcSrvIp, HostInfo.rpcSrvPort));
                }
            })).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            clientGroup.shutdownGracefully();
        }
    }

    public static class SimpleChatClientInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
            ChannelPipeline pipeline = socketChannel.pipeline();
            pipeline.addLast(new LengthFieldBasedFrameDecoder(65535, 0, 2, 0, 2));
            pipeline.addLast(new LengthFieldPrepender(2));
            pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));
            pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8));
            pipeline.addLast(new SimpleChatClientHandler());
        }
    }

    public static class SimpleChatClientHandler extends SimpleChannelInboundHandler<String> {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            new Thread(() -> {
                Scanner scanner = new Scanner(System.in);
                scanner.useDelimiter("\n");

                while (scanner.hasNext()) {
                    String line = scanner.next();
                    ctx.writeAndFlush(line);

                    if ("exit".equalsIgnoreCase(line)) {
                        break;
                    }
                }

                ctx.close();
            }).start();
        }

        @Override
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
            System.out.println(s);
        }
    }
}
