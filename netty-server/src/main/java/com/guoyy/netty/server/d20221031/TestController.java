package com.guoyy.netty.server.d20221031;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 1、执行NettyServerApplication
 * 2、http://websocket.jsonin.com/
 * ws://10.161.122.88:8888/webSocket
 * 连接服务器 发送消息:{"uid":"12345"}
 * 3、浏览器: http://localhost:8080/push/12345
 *
 * @author guoyiyong/james
 * Date: 2022/10/31 14:28
 * Version: 1.0
 *
 * Copyright (C) 2022 JW All rights reserved.
 */
@RestController
@RequestMapping("/push")
public class TestController {
    @Resource
    private PushMsgService pushMsgService;

    @GetMapping("/{uid}")
    public void pushOne(@PathVariable("uid") String uid) {
        pushMsgService.pushMsgToOne(uid, "hello:" + uid);
    }

}
