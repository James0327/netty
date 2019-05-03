package com.guoyy.netty.dto;

import io.netty.channel.ChannelHandlerContext;
import org.msgpack.annotation.Message;

@Message
public class Pair {
    private ChannelHandlerContext ctx;
    private String jsonStr;

    public Pair(ChannelHandlerContext ctx, String jsonStr) {
        this.ctx = ctx;
        this.jsonStr = jsonStr;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public String getJsonStr() {
        return jsonStr;
    }

    public void setJsonStr(String jsonStr) {
        this.jsonStr = jsonStr;
    }
}
