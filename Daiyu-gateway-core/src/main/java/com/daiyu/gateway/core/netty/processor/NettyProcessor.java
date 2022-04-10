package com.daiyu.gateway.core.netty.processor;

import com.daiyu.gateway.core.context.HttpRequestWrapper;

/**
 * @author ：chenlong
 * @filename NettyProcessor
 * @date 2022-04-10 10:08
 * @description：处理netty核心逻辑的执行器接口定义
 */
public interface NettyProcessor {
    /**
     * 核心执行方法
     */
    public void process(HttpRequestWrapper httpRequestWrapper);

    /**
     * 执行器启动
     */
    public void start();

    /**
     * 执行器关闭
     */
    public void shutdown();


}
