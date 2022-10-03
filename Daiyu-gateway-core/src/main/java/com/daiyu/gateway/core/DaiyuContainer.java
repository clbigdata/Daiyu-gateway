package com.daiyu.gateway.core;

import com.daiyu.common.constants.DaiyuBufferHelper;
import com.daiyu.gateway.core.netty.NettyHttpClient;
import com.daiyu.gateway.core.netty.NettyHttpServer;
import com.daiyu.gateway.core.netty.processor.NettyBatchEventProcessor;
import com.daiyu.gateway.core.netty.processor.NettyCoreProcessor;
import com.daiyu.gateway.core.netty.processor.NettyMpmcProcessor;
import com.daiyu.gateway.core.netty.processor.NettyProcessor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ：chenlong
 * @filename DaiyuContainer
 * @date 2022-04-05 10:55
 * @description：主流程测容器类
 */
@Slf4j
public class DaiyuContainer implements LifeCycle {
    private DaiyuGatewayConfig gatewayConfig;//核心配置累
    private NettyProcessor nettyProcessor;
    private NettyHttpServer nettyHttpServer;

    private NettyHttpClient nettyHttpClient;

    public DaiyuContainer(DaiyuGatewayConfig gatewayConfig) {
        this.gatewayConfig = gatewayConfig;
        init();
    }

    @Override
    public void init() {
        //1.构建核心处理器
        NettyCoreProcessor nettyCoreProcessor = new NettyCoreProcessor();
        //2.是否开启缓冲
        String bufferType = gatewayConfig.getBufferType();

        if (DaiyuBufferHelper.isFlusher(bufferType)) {
            nettyProcessor = new NettyBatchEventProcessor(gatewayConfig, nettyCoreProcessor);
        } else if (DaiyuBufferHelper.isMpmc(bufferType)) {
            nettyProcessor = new NettyMpmcProcessor(gatewayConfig, nettyCoreProcessor,false);
        } else {
            nettyProcessor = nettyCoreProcessor;
        }
        //3.创建NettyHttpProcessor
        nettyHttpServer = new NettyHttpServer(gatewayConfig, nettyProcessor);

        //4.创建nettyHttpclient
        nettyHttpClient = new NettyHttpClient(gatewayConfig, nettyHttpServer.getEventLoopGroupWork());

    }

    @Override
    public void start() {
        nettyProcessor.start();
        nettyHttpServer.start();
        log.info("容器启动！");
    }

    @Override
    public void shutdown() {
        nettyProcessor.shutdown();
        nettyHttpServer.shutdown();
    }
}
