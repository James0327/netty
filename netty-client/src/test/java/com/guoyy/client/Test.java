package com.guoyy.client;

public class Test {

    private static class Node {
        int i;
        Node next;
    }

    public static void main(String[] args) {
        Node node = init();
        display(node);
        System.out.println("====================");

        node = reverse(node);

        display(node);
    }

    private static Node reverse(Node node) {
        Node p = null;
        Node q = node;
        Node n = q.next;

        while (n != null) {
            q.next = p;
            p = q;
            q = n;
            n = q.next;
        }
        q.next = p;

        return q;
    }

    private static Node init() {
        Node node5 = new Node();
        node5.i = 5;
        Node node4 = new Node();
        node4.i = 4;
        node4.next = node5;
        Node node3 = new Node();
        node3.i = 3;
        node3.next = node4;
        Node node2 = new Node();
        node2.i = 2;
        node2.next = node3;
        Node node1 = new Node();
        node1.i = 1;
        node1.next = node2;

        return node1;
    }

    private static void display(Node n) {
        System.out.println(n + "\t" + n.i);
        Node next = n.next;
        if (next != null && next != n) {
            display(next);
        }
    }

}
