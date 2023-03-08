package com.guoyy.netty.server.d20221103.bean;

import com.guoyy.netty.server.d20221103.ProviderConfig;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Description: netty
 * com.guoyy.netty.server.d20221103.bean.ProviderBean
 *
 * @author guoyiyong/james
 * Date: 2022/11/3 17:54
 * Version: 1.0
 *
 * Copyright (C) 2022 JW All rights reserved.
 */
public class ProviderBean extends ProviderConfig implements ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        doExport();
    }
}
