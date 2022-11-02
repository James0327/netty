package com.guoyy.netty.server.d20221031;

/**
 * Description: 推送消息接口及实现类
 * com.guoyy.netty.server.d20221031.PushMsgService
 *
 * @author guoyiyong/james
 * Date: 2022/10/31 14:25
 * Version: 1.0
 *
 * Copyright (C) 2022 JW All rights reserved.
 */
public interface PushMsgService {
    /**
     * 推送给指定用户
     */
    void pushMsgToOne(String userId, String msg);

    /**
     * 推送给所有用户
     */
    void pushMsgToAll(String msg);
}
