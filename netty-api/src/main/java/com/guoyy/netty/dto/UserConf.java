package com.guoyy.netty.dto;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.msgpack.annotation.Message;

/**
 * com.guoyy.netty.dto
 *
 * @description:
 * @author: guoyiyong
 * @date: 2019/2/16
 * @time: 21:00
 * @@ Copyright (C) 2018 MTDP All rights reserved
 */
@Message
public class UserConf {
    private int uId;
    private int type;

    public int getuId() {
        return uId;
    }

    public void setuId(int uId) {
        this.uId = uId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
