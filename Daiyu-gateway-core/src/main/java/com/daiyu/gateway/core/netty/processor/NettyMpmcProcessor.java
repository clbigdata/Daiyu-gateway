package com.daiyu.gateway.core.netty.processor;

import com.daiyu.gateway.core.DaiyuGatewayConfig;
import com.daiyu.gateway.core.context.HttpRequestWrapper;

/**
 * @author ：chenlong
 * @filename NettyMpmcProcessor
 * @date 2022-04-10 11:24
 * @description：
 */
public class NettyMpmcProcessor implements NettyProcessor{
    private DaiyuGatewayConfig gatewayConfig;
    private NettyCoreProcessor nettyCoreProcessor;

    public NettyMpmcProcessor(DaiyuGatewayConfig gatewayConfig, NettyCoreProcessor nettyCoreProcessor) {
        this.gatewayConfig = gatewayConfig;
        this.nettyCoreProcessor = nettyCoreProcessor;
    }

    @Override
    public void process(HttpRequestWrapper httpRequestWrapper) {
        System.out.println("mpmc");
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }
}
