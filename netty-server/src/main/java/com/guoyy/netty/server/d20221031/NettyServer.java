package com.guoyy.netty.server.d20221031;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 * Description: netty服务器
 * com.guoyy.netty.server.d20221031.NettyServer
 *
 * @author guoyiyong/james
 * Date: 2022/10/31 14:11
 * Version: 1.0
 *
 * Copyright (C) 2022 JW All rights reserved.
 */
@Component
public class NettyServer {
    private static final Logger log = LoggerFactory.getLogger(NettyServer.class);

    /**
     * 端口号
     */
    @Value("${webSocket.netty.port:8888}")
    private int port;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workGroup;

    private final ProjectInitializer nettyInitializer;

    @Autowired
    public NettyServer(ProjectInitializer nettyInitializer) {
        this.nettyInitializer = nettyInitializer;
    }

    @PostConstruct
    public void start() {
        CompletableFuture.runAsync(() -> {
            bossGroup = new NioEventLoopGroup();
            workGroup = new NioEventLoopGroup();
            ServerBootstrap bootstrap = new ServerBootstrap();
            // bossGroup辅助客户端的tcp连接请求, workGroup负责与客户端之前的读写操作
            bootstrap.group(bossGroup, workGroup);
            // 设置NIO类型的channel
            bootstrap.channel(NioServerSocketChannel.class);
            // 设置监听端口
            bootstrap.localAddress(new InetSocketAddress(port));
            // 设置管道
            bootstrap.childHandler(nettyInitializer);

            // 配置完成，开始绑定server，通过调用sync同步方法阻塞直到绑定成功
            ChannelFuture channelFuture = null;
            try {
                channelFuture = bootstrap.bind().sync();
                log.info("Server started and listen on:{}", channelFuture.channel().localAddress());
                // 对关闭通道进行监听
                channelFuture.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 释放资源
     */
    @PreDestroy
    public void destroy() throws InterruptedException {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully().sync();
        }
        if (workGroup != null) {
            workGroup.shutdownGracefully().sync();
        }
    }

}
