package com.guoyy.netty.server.d20221103;

import lombok.Data;

/**
 * Description: netty
 * com.guoyy.netty.server.d20221103.ProviderConfig
 *
 * @author guoyiyong/james
 * Date: 2022/11/3 17:51
 * Version: 1.0
 *
 * Copyright (C) 2022 JW All rights reserved.
 */
@Data
public class ProviderConfig {
    private String nozzle;
    private String ref;
    private String alias;

    /**
     * 发布ø
     */
    protected void doExport() {
        System.out.format("生产者信息=> [接口：%s] [映射：%s] [别名：%s] \r\n", nozzle, ref, alias);
    }
}
