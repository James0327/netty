package com.guoyy.netty.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
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
 * com.guoyy.netty.server.MyHttpServer
 *
 * @author guoyiyong/james
 * Date: 2022/11/2 12:09
 * Version: 1.0
 *
 * Copyright (C) 2022 JW All rights reserved.
 */
public class MyHttpServer {

    public static void main(String[] args) throws IOException {
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
                String requestMethod = httpExchange.getRequestMethod();
                System.out.println("requestMethod:" + requestMethod);
                InputStream is = httpExchange.getRequestBody();
                int available = is.available();
                System.out.println(available);
                byte[] body = new byte[available];
                int read = is.read(body);
                System.out.println(read + "/" + new String(body));
                is.close();

                String response = "ok";

                Headers headers = httpExchange.getResponseHeaders();
                headers.set("Content-Type", "application/json; charset=utf-8");
                /*headers.set("Access-Control-Allow-Origin", "*");
                headers.set("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
                headers.set("Access-Control-Allow-Headers", "Origin,X-Requested-With,Content-Type,Accept");*/

                // 设置响应码200和响应body长度，这里我们设0，没有响应体
                httpExchange.sendResponseHeaders(200, response.length());

                OutputStream os = httpExchange.getResponseBody();
                os.write(response.getBytes());
                os.flush();
                os.close();

                httpExchange.close();
            }
        });

        ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1,
                0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(10),
                new BasicThreadFactory.Builder().build(), new ThreadPoolExecutor.AbortPolicy());
        httpServer.setExecutor(executor);

        httpServer.start();

        System.out.println("OK");
    }
}
