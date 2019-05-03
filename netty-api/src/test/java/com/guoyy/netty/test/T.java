package com.guoyy.netty.test;

import com.guoyy.netty.dto.UserInfo;
import com.guoyy.netty.util.MessageQueue;

import java.util.concurrent.locks.ReentrantLock;

/**
 * PACKAGE_NAME
 *
 * @description:
 * @author: guoyiyong
 * @date: 2019/2/17
 * @time: 21:23
 * @@ Copyright (C) 2018 MTDP All rights reserved
 */
public class T {
    public static void main(String[] args) throws InterruptedException {

        UserInfo userInfo = new UserInfo();

        MessageQueue.put(userInfo);

        MessageQueue.take();


        ReentrantLock lock = new ReentrantLock();
        try{
            lock.lock();



        }finally {
            lock.unlock();
        }




    }
}
