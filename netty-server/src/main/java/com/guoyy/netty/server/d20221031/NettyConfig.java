package com.guoyy.netty.server.d20221031;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Description: Netty配置
 * 管理全局Channel以及用户对应的channel（推送消息）
 *
 * com.guoyy.netty.server.d20221031.NettyConfig
 *
 * @author guoyiyong/james
 * Date: 2022/10/31 14:15
 * Version: 1.0
 *
 * Copyright (C) 2022 JW All rights reserved.
 */
public class NettyConfig {

    /**
     * 定义全局单利channel组 管理所有channel
     */
    private static volatile ChannelGroup channelGroup = null;

    /**
     * 存放请求ID与channel的对应关系
     */
    private static volatile ConcurrentHashMap<String, Channel> channelMap = null;

    /**
     * 定义两把锁
     */
    private static final Object lock1 = new Object();
    private static final Object lock2 = new Object();

    public static ChannelGroup getChannelGroup() {
        if (null == channelGroup) {
            synchronized (lock1) {
                if (null == channelGroup) {
                    channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
                }
            }
        }
        return channelGroup;
    }

    public static ConcurrentHashMap<String, Channel> getChannelMap() {
        if (null == channelMap) {
            synchronized (lock2) {
                if (null == channelMap) {
                    channelMap = new ConcurrentHashMap<>();
                }
            }
        }
        return channelMap;
    }

    public static Channel getChannel(String userId) {
        if (null == channelMap) {
            return getChannelMap().get(userId);
        }
        return channelMap.get(userId);
    }
}
