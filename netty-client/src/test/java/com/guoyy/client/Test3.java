package com.guoyy.client;

import java.util.Stack;

public class Test3 {
    public static void main(String[] args) {

        String src = "abC;!Def;(wQSx";
        String dest = reverse(src);

        System.out.println(src);
        System.out.println(dest);
    }

    private static String reverse(String src) {
        char[] sChs = src.toCharArray();
        char[] dChs = new char[sChs.length];
        Stack<Character> s = new Stack<>();
        int idx = 0;

        for (int i = 0, len = sChs.length; i < len; i++) {
            if ((sChs[i] >= 65 && sChs[i] <= 90)
                    || (sChs[i] >= 97 && sChs[i] <= 122)) {
                s.push(sChs[i]);
            } else {
                while (!s.isEmpty()) {
                    dChs[idx++] = s.pop();
                }
                dChs[idx++] = sChs[i];
            }
        }
        while (!s.isEmpty()) {
            dChs[idx++] = s.pop();
        }

        return new String(dChs);
    }
}
