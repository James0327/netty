package com.guoyy.client;

public class Test2 {

    public static void main(String[] args) {
        int s = sum(1, 1, 0, 7);
        System.out.println("s: " + s);
    }

    private static int sum(int a, int b, int c, int cnt) {
        System.out.println(String.format("a:%d,b:%d,c:%d,cnt:%d.", a, b, c, cnt));

        if (cnt == 2) {
            return c;
        }
        c = a + b;
        a = b;
        b = c;

        return sum(a, b, c, --cnt);
    }

}
