package com.guoyy.netty.client;

import com.alibaba.fastjson.JSON;
import com.guoyy.netty.conf.HostInfo;
import com.guoyy.netty.dto.UserInfo;
import com.guoyy.netty.util.MsgpackSerialize;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Description: netty
 * com.guoyy.netty.client.NettyHttpClient
 *
 * @author guoyiyong/james
 * Date: 2022/11/1 21:36
 * Version: 1.0
 *
 * Copyright (C) 2022 JW All rights reserved.
 */
public class NettyHttpClient {

    /**
     * POST localhost:8888/test
     * {"name":"123"}
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        /**
         * 1.HttpServer：表示一个服务器实例，需要绑定一个IP地址和端口号。（HttpsServer是其子类，处理https请求）
         * 2.HttpContext：服务器监听器的上下文，需要配置用于匹配URI的公共路径和用来处理请求的HttpHandler
         * （可以创建多个 HttpContext，一个 HttpContext 对应一个 HttpHandler，不同的 URI 请求，根据添加的 HttpContext 监听器，分配到对应的 HttpHandler 处理请求）
         * 3.HttpHandler:上下文对应的http请求处理器
         * 4.HttpExchange：监听器回调时传入的参数，封装了http请求和响应的所有数据操作
         */
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(8888), 0);
        httpServer.createContext("/test", new HttpHandler() {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException {
                InputStream is = httpExchange.getRequestBody();
                int available = is.available();
                byte[] body = new byte[available];
                int read = is.read(body);
                String msg = new String(body);
                System.out.println(read + "/" + msg);

                if (CHANNEL != null) {
                    // 这里写String 接收方按String解析
                    CHANNEL.writeAndFlush(msg);
                }
                is.close();

                HTTP_EXCHANGE = httpExchange;

                System.out.println("xxxxxx");
            }
        });
        httpServer.setExecutor(new ThreadPoolExecutor(1, 1,
                0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(10),
                new BasicThreadFactory.Builder().namingPattern("thread-%d").build(),
                new ThreadPoolExecutor.DiscardPolicy()));
        httpServer.start();

        NioEventLoopGroup clientGroup = new NioEventLoopGroup(1);

        Bootstrap bootstrap = new Bootstrap();
        try {
            ChannelFuture channelFuture = bootstrap.group(clientGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 600)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new LoggingHandler(LogLevel.INFO));
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(65535, 0, 2, 0, 2));
                            pipeline.addLast(new LengthFieldPrepender(2));
                            pipeline.addLast(new MsgpackSerialize.Encoder());
                            pipeline.addLast(new MsgpackSerialize.Decoder());
                            pipeline.addLast(new WriteTimeoutHandler(120));
                            pipeline.addLast(new NettyHttpClientHandler());
                        }
                    }).connect(HostInfo.rpcSrvIp, HostInfo.rpcSrvPort)
                    .addListener(future -> {
                        if (future.isSuccess()) {
                            System.out.printf("已和服务器[%s:%d]建立连接，可以正常进行业务处理。%n", HostInfo.rpcSrvIp, HostInfo.rpcSrvPort);
                        }
                    }).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            clientGroup.shutdownGracefully();
        }

    }

    private static class NettyHttpClientHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            System.out.println("client channelRead " + ctx);

            UserInfo userInfo = (UserInfo)msg;
            userInfo.setTimes(1);

            byte[] response = JSON.toJSONBytes(userInfo);
            HTTP_EXCHANGE.sendResponseHeaders(200, response.length);
            OutputStream os = HTTP_EXCHANGE.getResponseBody();
            os.write(response);
            os.flush();
            os.close();

            HTTP_EXCHANGE.close();

            System.out.println("~~~~~~~~~");

            ReferenceCountUtil.release(msg);
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
            System.out.println("handlerAdded: " + ctx);
            CHANNEL = ctx.channel();
        }
    }

    private static HttpExchange HTTP_EXCHANGE;
    private static Channel CHANNEL;
}
