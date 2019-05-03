package com.guoyy.netty.util;

public class Singleton2 {

    private final static Singleton2 INSTANCE;

    private Singleton2() {
    }

    static {
        INSTANCE = new Singleton2();
    }

    public static Singleton2 newInstance() {
        return INSTANCE;
    }
}
