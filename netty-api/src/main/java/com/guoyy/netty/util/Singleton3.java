package com.guoyy.netty.util;

public class Singleton3 {

    private final static Singleton3 INSTANCE = new Singleton3();

    private Singleton3() {
    }

    public static Singleton3 newInstance() {
        return INSTANCE;
    }
}
