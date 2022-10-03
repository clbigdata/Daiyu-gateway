package com.daiyu.gateway.core.netty.processor;

import com.daiyu.common.consurrent.queue.flusher.ParallelFlusher;
import com.daiyu.common.enums.ResponseCode;
import com.daiyu.gateway.core.DaiyuGatewayConfig;
import com.daiyu.gateway.core.context.HttpRequestWrapper;
import com.daiyu.gateway.core.helper.ResponseHelper;
import com.lmax.disruptor.dsl.ProducerType;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ：chenlong
 * @filename NettyBatchEventProcessor
 * @date 2022-04-10 11:21
 * @description：
 */
@Slf4j
public class NettyBatchEventProcessor implements NettyProcessor {
    private DaiyuGatewayConfig gatewayConfig;
    private NettyCoreProcessor nettyCoreProcessor;
    private static final String THREAD_NAME_PREFIX = "daiyu-flusher";
    private ParallelFlusher<HttpRequestWrapper> parallelFlusher;

    public NettyBatchEventProcessor(DaiyuGatewayConfig gatewayConfig, NettyCoreProcessor nettyCoreProcessor) {
        this.gatewayConfig = gatewayConfig;
        this.nettyCoreProcessor = nettyCoreProcessor;
        ParallelFlusher.Builder<HttpRequestWrapper> builder = new ParallelFlusher.Builder<HttpRequestWrapper>()
                .setBufferSize(gatewayConfig.getBufferSize())
                .setThreads(gatewayConfig.getProcessThread())
                .setProducerType(ProducerType.MULTI)
                .setNamePreFix(THREAD_NAME_PREFIX)
                .setWaitStrategy(gatewayConfig.getATrueWaitStrategy());

        BatchEventProcessListener batchEventProcessListener = new BatchEventProcessListener();
        builder.setEventListener(batchEventProcessListener);

        this.parallelFlusher = builder.build();

    }

    @Override
    public void process(HttpRequestWrapper httpRequestWrapper) {
        this.parallelFlusher.add(httpRequestWrapper);
    }

    @Override
    public void start() {
        this.nettyCoreProcessor.start();
        this.parallelFlusher.start();
    }

    @Override
    public void shutdown() {
        this.nettyCoreProcessor.shutdown();
        this.parallelFlusher.shutDown();
    }

    /**
     * 监听事件的核心处理逻辑
     */
    public class BatchEventProcessListener implements ParallelFlusher.EventListener<HttpRequestWrapper> {

        @Override
        public void onEvent(HttpRequestWrapper event) throws Exception {
            nettyCoreProcessor.process(event);
        }

        @Override
        public void onException(Throwable t, long sequence, HttpRequestWrapper event) {
            HttpRequest httpRequest = event.getFullHttpRequest();
            ChannelHandlerContext ctx = event.getCtx();
            try {
                log.error("#BatchEventProcessorListener! onException请求处理失败，request:{}", t.getMessage());

                FullHttpResponse httpResponse = ResponseHelper.getHttpResponse(ResponseCode.INTERNAL_ERROR);
                //判断是否保持联结
                if (!HttpUtil.isKeepAlive(httpRequest)) {
                    ctx.writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE);
                } else {
                    //如果保持连接，则需要设置一下响应头
                    httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                    ctx.writeAndFlush(httpResponse);
                }
            } catch (Exception e) {
                log.error("#BatchEventProcessorListener! onException请求回写失败，request:{}", e.getMessage());
            }
        }
    }

    public DaiyuGatewayConfig daiyuGatewayConfig() {
        return gatewayConfig;
    }
}
