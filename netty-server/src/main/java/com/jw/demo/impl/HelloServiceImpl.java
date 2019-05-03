package com.jw.demo.impl;

import com.jw.demo.HelloService;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.time.LocalDateTime;

public class HelloServiceImpl implements HelloService.Iface {
    @Override
    public String sayHello(String para) {
        try {
            return String.format("IP:[%s],Time:[%s],Msg:[%s].", Inet4Address.getLocalHost(), LocalDateTime.now(), para);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }
}
