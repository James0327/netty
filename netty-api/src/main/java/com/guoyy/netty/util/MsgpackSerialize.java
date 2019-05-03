package com.guoyy.netty.util;

import com.guoyy.netty.dto.Pair;
import com.guoyy.netty.dto.UserConf;
import com.guoyy.netty.dto.UserInfo;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.msgpack.MessagePack;

import java.util.List;

/**
 * com.guoyy.netty.util
 *
 * @description:
 * @author: guoyiyong
 * @date: 2019/2/16
 * @time: 20:10
 * @@ Copyright (C) 2018 MTDP All rights reserved
 */
public class MsgpackSerialize {
    private static final MessagePack pack = new MessagePack();

    public static class Encoder extends MessageToByteEncoder<Object> {
        @Override
        protected void encode(ChannelHandlerContext channelHandlerContext, Object obj, ByteBuf out) throws Exception {
//            MessagePack pack = new MessagePack();
            byte[] bytes = pack.write(obj);
            final byte type;
            if (obj instanceof UserInfo) {
                type = 1;
            } else if (obj instanceof UserConf) {
                type = 2;
            } else if (obj instanceof Pair) {
                type = 3;
            } else {
                type = 9;
            }

            out.writeByte(type);
            out.writeBytes(bytes);
        }
    }

    public static class Decoder extends MessageToMessageDecoder<ByteBuf> {
        @Override
        protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf buf, List<Object> out) throws Exception {
            int len = buf.readableBytes();
            byte type = buf.readByte();
            byte[] bytes = new byte[len - 1];
            buf.getBytes(buf.readerIndex(), bytes);

//            MessagePack pack = new MessagePack();
            if (type == 1) {
                UserInfo userInfo = pack.read(bytes, UserInfo.class);
                out.add(userInfo);
            } else if (type == 2) {
                UserConf userConf = pack.read(bytes, UserConf.class);
                out.add(userConf);
            } else if (type == 3) {
                Pair pair = pack.read(bytes, Pair.class);
                out.add(pair);
            } else {
                String obj = pack.read(bytes, String.class);
                out.add(obj);
            }
        }
    }
}
