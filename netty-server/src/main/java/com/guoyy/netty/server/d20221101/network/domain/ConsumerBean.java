package com.guoyy.netty.server.d20221101.network.domain;

import com.alibaba.fastjson2.JSON;
import io.netty.channel.ChannelFuture;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.util.Assert;

/**
 * Description: netty
 * com.guoyy.netty.server.d20221101.network.domain.ConsumerBean
 *
 * @author guoyiyong/james
 * Date: 2022/11/4 15:48
 * Version: 1.0
 *
 * Copyright (C) 2022 JW All rights reserved.
 */
public class ConsumerBean<T> extends ConsumerConfig<T> implements FactoryBean {

    private ChannelFuture channelFuture;

    private RpcProviderConfig rpcProviderConfig;

    @Override
    public Object getObject() throws Exception {

        //从redis获取链接
        if (null == rpcProviderConfig) {
            String infoStr = RedisRegistryCenter.obtainProvider(nozzle, alias);
            rpcProviderConfig = JSON.parseObject(infoStr, RpcProviderConfig.class);
        }
        Assert.isTrue(null != rpcProviderConfig);

        //获取通信channel
        if (null == channelFuture) {
            ClientSocket clientSocket = new ClientSocket(rpcProviderConfig.getHost(), rpcProviderConfig.getPort());
            new Thread(clientSocket).start();
            for (int i = 0; i < 100; i++) {
                if (null != channelFuture)
                    break;
                Thread.sleep(500);
                channelFuture = clientSocket.getFuture();
            }
        }
        Assert.isTrue(null != channelFuture);

        Request request = new Request();
        request.setChannel(channelFuture.channel());
        request.setNozzle(nozzle);
        request.setRef(rpcProviderConfig.getRef());
        request.setAlias(alias);
        return (T)JDKProxy.getProxy(ClassLoaderUtils.forName(nozzle), request);
    }

    @Override
    public Class<?> getObjectType() {
        try {
            return ClassLoaderUtils.forName(nozzle);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
