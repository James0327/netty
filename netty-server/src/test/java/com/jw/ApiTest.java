package com.jw;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Description: netty
 * com.jw.ApiTest
 *
 * @author guoyiyong/james
 * Date: 2022/11/3 20:43
 * Version: 1.0
 *
 * Copyright (C) 2022 JW All rights reserved.
 */
public class ApiTest {

    public static void main(String[] args) {
        String[] configs = {"rpc-consumer.xml", "rpc-provider.xml"};
        new ClassPathXmlApplicationContext(configs);
    }

}
