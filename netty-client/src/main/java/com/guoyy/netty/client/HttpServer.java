package com.guoyy.netty.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.guoyy.netty.conf.HostInfo;
import com.guoyy.netty.util.MessageQueue;
import com.guoyy.netty.util.MsgpackSerialize;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

import java.util.List;
import java.util.Map;

/**
 * com.guoyy.netty.http.server
 *
 * @description:
 * @author: guoyiyong
 * @date: 2019/2/16
 * @time: 19:40
 * @@ Copyright (C) 2018 MTDP All rights reserved
 */
public class HttpServer {

    public static void main(String[] args) {
        // Configure the server.
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap server = new ServerBootstrap();
            server.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new HttpRequestDecoder());
                            pipeline.addLast(new HttpResponseEncoder());
                            // HttpObjectAggregator 解码器的作用是将多条消息转换为单一的FullHttpRequest/FullHttpResponse
                            // 原因是http解码器在每个http消息中会产生多个对象：HttpRequest/HttpResponse + HttpContent + LastHttpContent
                            pipeline.addLast(new HttpObjectAggregator(65535));
                            // 支持异步发送大的码流（例如大文件传输），但不占用过多内存，以防止java内存溢出
                            pipeline.addLast(new ChunkedWriteHandler());
                            pipeline.addLast(new WriteTimeoutHandler(120));
                            pipeline.addLast(new HttpServerHandler());
                        }
                    }).option(ChannelOption.SO_REUSEADDR, true)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture channelFuture = server.bind(HostInfo.rpcCliIp, HostInfo.rpcCliPort).addListener(future -> {
                if (future.isSuccess()) {
                    System.err.println(String.format("Open your web browser and navigate to  http://%s:%d/",
                            HostInfo.rpcCliIp, HostInfo.rpcCliPort));
                }
            }).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    private static class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
        @Override
        public void channelActive(ChannelHandlerContext ctx0) {
            System.out.println("HttpServerHandler channelActive");

            new Thread(() -> {
                NioEventLoopGroup clientGroup = new NioEventLoopGroup(1);
                try {
                    Bootstrap client = new Bootstrap();
                    client.group(clientGroup)
                            .channel(NioSocketChannel.class)
                            .handler(new ChannelInitializer<SocketChannel>() {
                                         @Override
                                         protected void initChannel(SocketChannel socketChannel) throws Exception {
                                             ChannelPipeline pipeline = socketChannel.pipeline();
                                             pipeline.addLast(new LengthFieldBasedFrameDecoder(65535, 0, 2, 0, 2));
                                             pipeline.addLast(new LengthFieldPrepender(2));
                                             pipeline.addLast(new MsgpackSerialize.Encoder());
                                             pipeline.addLast(new MsgpackSerialize.Decoder());
                                             pipeline.addLast(new WriteTimeoutHandler(120));
                                             pipeline.addLast(new TcpClientHandler(ctx0));
                                         }
                                     }
                            ).option(ChannelOption.SO_KEEPALIVE, true)
                            .option(ChannelOption.TCP_NODELAY, true);

                    ChannelFuture channelFuture = client.connect(HostInfo.rpcSrvIp, HostInfo.rpcSrvPort).addListener(future -> {
                        System.out.println(String.format("连接到服务器[%s:%d]，可以正常通讯。", HostInfo.rpcSrvIp, HostInfo.rpcSrvPort));
                    }).sync();
                    channelFuture.channel().closeFuture().sync();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    clientGroup.shutdownGracefully();
                }
            }).start();
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {

            System.out.println("HttpServerHandler channelRead0");

            boolean success = request.decoderResult().isSuccess();
            if (!success) {
                System.err.println("HttpServerHandler fail ...");
                return;
            }
            System.out.println(String.format("HttpMethod:[%s], URI:[%s].", request.method(), request.uri()));

            boolean keepAlive = HttpUtil.isKeepAlive(request);
            System.out.println("keepAlive: " + keepAlive);

            QueryStringDecoder stringDecoder = new QueryStringDecoder(request.uri());
            Map<String, List<String>> parameters = stringDecoder.parameters();
            System.out.println("parameters: " + parameters);

            HttpPostRequestDecoder postRequestDecoder = new HttpPostRequestDecoder(request);
            List<InterfaceHttpData> bodyHttpDatas = postRequestDecoder.getBodyHttpDatas();
            System.out.println("bodyHttpDatas: " + bodyHttpDatas);

            ByteBuf buf = request.content();
            int len = buf.readableBytes();
            byte[] bytes = new byte[len];
            buf.getBytes(buf.readerIndex(), bytes, 0, len);

            String jsonStr = new String(bytes, CharsetUtil.UTF_8);

            MessageQueue.put(jsonStr);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }

    private static class TcpClientHandler extends ChannelInboundHandlerAdapter {
        private static final AsciiString CONTENT_TYPE = AsciiString.cached("Content-Type");
        private static final AsciiString CONTENT_LENGTH = AsciiString.cached("Content-Length");
        private static final AsciiString CONNECTION = AsciiString.cached("Connection");
        private static final AsciiString KEEP_ALIVE = AsciiString.cached("keep-alive");

        private ChannelHandlerContext ctx0;

        public TcpClientHandler(ChannelHandlerContext ctx0) {
            this.ctx0 = ctx0;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("TcpClientHandler channelActive.");

            String jsonStr = (String) MessageQueue.take();
            JSONObject jsonObj = JSON.parseObject(jsonStr);
            jsonObj.put("HttpServerTime", System.currentTimeMillis());

            ctx.writeAndFlush(jsonObj.toJSONString());
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            try {
                System.out.println("TcpClientHandler channelRead msg: " + msg);

                ByteBuf byteBuf = Unpooled.copiedBuffer(msg.toString(), CharsetUtil.UTF_8);

                DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, byteBuf);

                response.headers().set(CONTENT_TYPE, "applicationon/json;charset=UTF-8");
                response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
                response.headers().set(KEEP_ALIVE, true);

                ctx0.writeAndFlush(response);

                ctx.close();
                ctx0.close();
            } finally {
                ReferenceCountUtil.release(msg);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }
    }

}
