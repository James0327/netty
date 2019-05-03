package com.guoyy.netty.util;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * com.guoyy.netty.util
 *
 * @description:
 * @author: guoyiyong
 * @date: 2019/2/17
 * @time: 21:16
 * @@ Copyright (C) 2018 MTDP All rights reserved
 */
public enum MessageQueue {
    ;
    private static LinkedBlockingDeque queue = new LinkedBlockingDeque(8192);

    public static Object take() throws InterruptedException {
        return queue.take();
    }

    public static void put(Object obj) throws InterruptedException {
        queue.put(obj);
    }
}
