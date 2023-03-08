package com.guoyy.netty.server.d20221103;

import com.guoyy.netty.server.d20221103.bean.ConsumerBean;
import com.guoyy.netty.server.d20221103.bean.ProviderBean;
import com.guoyy.netty.server.d20221103.bean.ServerBean;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Description: netty
 * com.guoyy.netty.server.d20221103.MyNamespaceHandler
 *
 * @author guoyiyong/james
 * Date: 2022/11/3 17:57
 * Version: 1.0
 *
 * Copyright (C) 2022 JW All rights reserved.
 */
public class MyNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("consumer", new MyBeanDefinitionParser(ConsumerBean.class));
        registerBeanDefinitionParser("provider", new MyBeanDefinitionParser(ProviderBean.class));
        registerBeanDefinitionParser("server", new MyBeanDefinitionParser(ServerBean.class));
    }

}
