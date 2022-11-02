package com.guoyy.netty.server.d20221031;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Description: netty
 * com.guoyy.netty.server.d20221031.PushMsgServiceImpl
 *
 * @author guoyiyong/james
 * Date: 2022/10/31 14:26
 * Version: 1.0
 *
 * Copyright (C) 2022 JW All rights reserved.
 */
@Service
public class PushMsgServiceImpl implements PushMsgService {

    @Override
    public void pushMsgToOne(String userId, String msg) {
        Channel channel = NettyConfig.getChannel(userId);
        if (Objects.isNull(channel)) {
            throw new RuntimeException("未连接socket服务器");
        }

        channel.writeAndFlush(new TextWebSocketFrame(msg));
    }

    @Override
    public void pushMsgToAll(String msg) {
        NettyConfig.getChannelGroup().writeAndFlush(new TextWebSocketFrame(msg));
    }
}
