package com.guoyy.netty.util;

public class Singleton1 {

    private Singleton1() { }

    private static class SingletonInstance {
        private static final Singleton1 INSTANCE = new Singleton1();
    }

    public static Singleton1 newInstance() {
        return SingletonInstance.INSTANCE;
    }
}
