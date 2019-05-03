package com.guoyy.netty.test ;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * com.guoyy.netty.util
 *
 * @description:
 * @author: guoyiyong
 * @date: 2019/2/17
 * @time: 15:58
 * @@ Copyright (C) 2018 MTDP All rights reserved
 */
public class ArrayBlockingQueue<E> {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ArrayBlockingQueue<Integer> queue = new ArrayBlockingQueue<>(3000);

        CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
            for (int i = 0; i < 200_000_000; i++) {
                try {
                    queue.put(i);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
            for (int i = 0; i < 200_000_000; i++) {
                try {
                    queue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        long start = System.currentTimeMillis();
        CompletableFuture.allOf(future1, future2).get();
        long end = System.currentTimeMillis();
        System.out.println(String.format("estimate:%s,start:%s,end:%s", end - start, start, end));

        Thread.currentThread().join(30_000);
    }

    private ReentrantLock lock = new ReentrantLock();
    private Condition notFull = lock.newCondition();
    private Condition notEmpty = lock.newCondition();
    private int putIdx, takeIdx, cnt;
    private final Object[] objs;
    private final int capacity;

    public ArrayBlockingQueue(int capacity) {
        this.capacity = capacity;
        this.objs = new Object[capacity];
    }

    public E take() throws InterruptedException {
        lock.lock();
        try {
            while (cnt == 0) {
                notEmpty.await();
            }
            E e = (E) objs[takeIdx++];
            if (takeIdx == capacity) {
                takeIdx = 0;
            }
            if (cnt == capacity) {
                notFull.signal();
            }
            cnt--;

            System.out.println(String.format("take obj:[%s] capacity:%d, cnt:%d, putIdx:%d, takeIdx:%d.", e, capacity, cnt, putIdx, takeIdx));

            return e;
        } finally {
            lock.unlock();
        }
    }

    public void put(E e) throws InterruptedException {
        lock.lock();
        try {
            while (cnt == capacity) {
                notFull.await();
            }
            objs[putIdx++] = e;
            if (putIdx == capacity) {
                putIdx = 0;
            }
            cnt++;
            if (cnt == 1) {
                notEmpty.signal();
            }

            System.out.println(String.format("put obj:[%s] capacity:%d, cnt:%d, putIdx:%d, takeIdx:%d.", e, capacity, cnt, putIdx, takeIdx));

        } finally {
            lock.unlock();
        }
    }

}
