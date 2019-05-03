package com.guoyy.netty.test;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.time.Duration;
import java.time.LocalTime;
import java.util.concurrent.*;

public class MinStack {

    private static class Stack {

        private final int[] items = new int[8192];
        private final int[] mins = new int[items.length];

        private int idx = 0;
        private int mIdx = 0;

        public void push(int in) {
            items[idx] = in;
            if (idx == 0) {
                mins[mIdx++] = in;
            } else {
                if (in <= min()) {
                    mins[mIdx++] = in;
                }
            }
            idx++;

            System.out.println("itmes:" + ToStringBuilder.reflectionToString(items));
            System.out.println(" mins:" + ToStringBuilder.reflectionToString(mins));

            System.out.println(String.format("idx:%d,mIdx:%d. min:[%d].", idx, mIdx, min()));
        }

        public int pop() {
            if (idx < 0) {
                return -1;
            }

            int item = items[--idx];
            System.out.println(String.format("itme:%d,min:%d.", item, min()));
            if (item == min()) {
                mIdx--;
            }

            items[idx] = 0;
            mins[mIdx] = 0;

            System.out.println("itmes:" + ToStringBuilder.reflectionToString(items));
            System.out.println(" mins:" + ToStringBuilder.reflectionToString(mins));

            System.out.println(String.format("idx:%d,mIdx:%d. min:[%d].", idx, mIdx, min()));

            return item;
        }

        public int min() {
            if (mIdx == 0) {
                return -1;
            }
            return mins[mIdx - 1];
        }

        public void print() {
            System.out.print("itmes:");
            for (int i = 0; i < idx; i++) {
                System.out.print(items[i] + ",");
            }
            System.out.println();

            System.out.print(" mins:");
            for (int i = 0; i < mIdx; i++) {
                System.out.print(mins[i] + ",");
            }
            System.out.println();
            System.out.println(String.format("idx:%d,mIdx:%d. min:[%d].", idx, mIdx, min()));
        }

    }


    private static String reverse(String src, int n) {
        char[] chs = src.toCharArray();

        char ch;
        for (int i = 0, cnt = n >>> 1; i < cnt; i++) {
            ch = chs[i];
            chs[i] = chs[n - i - 1];
            chs[n - i - 1] = ch;
        }
//        System.out.println(chs);

        for (int i = 0, cnt = (chs.length - n) >>> 1; i < cnt; i++) {
            ch = chs[n + i];
            chs[n + i] = chs[chs.length - i - 1];
            chs[chs.length - i - 1] = ch;
        }
//        System.out.println(chs);

        for (int i = 0, cnt = chs.length >>> 1; i < cnt; i++) {
            ch = chs[i];
            chs[i] = chs[chs.length - i - 1];
            chs[chs.length - i - 1] = ch;
        }
//        System.out.println(chs);

        return new String(chs);
    }

    public static void main(String[] args) {

        Stack stack = new Stack();

        stack.push(9);
        stack.push(3);
        stack.push(4);
        stack.push(3);
        stack.push(2);
        stack.push(2);

        stack.print();

        for (int i = 0; i < 6; i++) {
            stack.pop();
            //       stack.print();
        }

        System.exit(1);

        LocalTime start = LocalTime.now();

        int processors = Runtime.getRuntime().availableProcessors();

        ThreadPoolExecutor pool = new ThreadPoolExecutor(processors, processors * 2, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000_000), new ThreadPoolExecutor.AbortPolicy());

        String str = "abcdefg1234567890";
        int n = 7;

        CompletableFuture[] rets = new CompletableFuture[1000_000];

        for (int i = 0; i < 10_000_000; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                String ret = reverse(str, n);
//                System.out.println("ret: " + ret);
            }, pool);
            rets[i] = future;
        }

        CompletableFuture<Void> allOf = CompletableFuture.allOf(rets);
        try {
            allOf.get();

            LocalTime end = LocalTime.now();

            System.out.println(String.format("start:%s,end:%s,estimate time:%s.", start, end, Duration.between(start, end).toMillis()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } finally {
            pool.shutdown();
        }

    }

}
