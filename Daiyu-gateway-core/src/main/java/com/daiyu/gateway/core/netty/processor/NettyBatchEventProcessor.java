package com.daiyu.gateway.core.netty.processor;

import com.daiyu.gateway.core.DaiyuGatewayConfig;
import com.daiyu.gateway.core.context.HttpRequestWrapper;

/**
 * @author ：chenlong
 * @filename NettyBatchEventProcessor
 * @date 2022-04-10 11:21
 * @description：
 */
public class NettyBatchEventProcessor implements NettyProcessor{
    private DaiyuGatewayConfig gatewayConfig;
    private NettyCoreProcessor nettyCoreProcessor;

    public NettyBatchEventProcessor(DaiyuGatewayConfig gatewayConfig, NettyCoreProcessor nettyCoreProcessor) {
        this.gatewayConfig = gatewayConfig;
        this.nettyCoreProcessor = nettyCoreProcessor;
    }

    @Override
    public void process(HttpRequestWrapper httpRequestWrapper) {
        System.out.println("batch");

    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }
}
