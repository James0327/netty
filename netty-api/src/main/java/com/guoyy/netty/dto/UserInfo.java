package com.guoyy.netty.dto;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.msgpack.annotation.Message;

import java.util.Date;

/**
 * com.guoyy.netty.http
 *
 * @description:
 * @author: guoyiyong
 * @date: 2019/2/16
 * @time: 19:26
 * @@ Copyright (C) 2018 MTDP All rights reserved
 */
@Message
public class UserInfo {
    private int uId;
    private String name;
    private char[] password;
    private long lastLoginTime;
    private Date birthday;
    private boolean sex;
    private int age;
    private int times;
    private String remark;

    public int getuId() {
        return uId;
    }

    public void setuId(int uId) {
        this.uId = uId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public char[] getPassword() {
        return password;
    }

    public void setPassword(char[] password) {
        this.password = password;
    }

    public long getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(long lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public boolean isSex() {
        return sex;
    }

    public void setSex(boolean sex) {
        this.sex = sex;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
