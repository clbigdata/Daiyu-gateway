package com.daiyu.gateway.core.netty.processor;

import com.daiyu.common.consurrent.queue.mpmc.MpmcBlockingQueue;
import com.daiyu.common.enums.ResponseCode;
import com.daiyu.gateway.core.DaiyuGatewayConfig;
import com.daiyu.gateway.core.context.HttpRequestWrapper;
import com.daiyu.gateway.core.helper.ResponseHelper;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author ：chenlong
 * @filename NettyMpmcProcessor
 * @date 2022-04-10 11:24
 * @description：
 */
@Slf4j
public class NettyMpmcProcessor implements NettyProcessor {
    private DaiyuGatewayConfig gatewayConfig;
    private NettyCoreProcessor nettyCoreProcessor;
    private MpmcBlockingQueue<HttpRequestWrapper> mpmcBlockingQueue;
    private boolean usedExecutorPool;
    private ExecutorService executorService;
    private volatile boolean isRunning = false;
    private Thread consumerProcessorThread;

    public NettyMpmcProcessor(DaiyuGatewayConfig gatewayConfig, NettyCoreProcessor nettyCoreProcessor, boolean usedExecutorPool) {
        this.gatewayConfig = gatewayConfig;
        this.nettyCoreProcessor = nettyCoreProcessor;
        this.mpmcBlockingQueue = new MpmcBlockingQueue<>(gatewayConfig.getBufferSize());
        this.usedExecutorPool = usedExecutorPool;
    }

    @Override
    public void process(HttpRequestWrapper httpRequestWrapper) throws Exception {
        System.out.println("NettyMpmcProcessor put!");
        this.mpmcBlockingQueue.put(httpRequestWrapper);
    }

    @Override
    public void start() {
        this.isRunning = true;
        this.nettyCoreProcessor.start();
        if (usedExecutorPool) {
            this.executorService = Executors.newFixedThreadPool(gatewayConfig.getProcessThread());
            for (int i = 0; i < gatewayConfig.getProcessThread(); i++) {
                this.executorService.submit(new ConsumerProcessor());
            }
        } else {
            this.consumerProcessorThread = new Thread(new ConsumerProcessor());
            this.consumerProcessorThread.start();
        }
    }

    @Override
    public void shutdown() {
        this.isRunning = false;
        this.nettyCoreProcessor.shutdown();
        if (usedExecutorPool) {
            this.executorService.shutdown();
        }
    }

    /**
     * 消费者核心实现类
     */
    public class ConsumerProcessor implements Runnable {

        @Override
        public void run() {
            while (isRunning) {
                HttpRequestWrapper event = null;
                try {
                    event = mpmcBlockingQueue.take();
                    nettyCoreProcessor.process(event);
                } catch (Throwable t) {
                    if (event != null) {
                        FullHttpRequest request = event.getFullHttpRequest();
                        ChannelHandlerContext ctx = event.getCtx();
                        try {
                            log.error("#ConsumerProcessor! onException请求处理失败，request:{}", t.getMessage());

                            FullHttpResponse httpResponse = ResponseHelper.getHttpResponse(ResponseCode.INTERNAL_ERROR);
                            //判断是否保持联结
                            if (!HttpUtil.isKeepAlive(request)) {
                                ctx.writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE);
                            } else {
                                //如果保持连接，则需要设置一下响应头
                                httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                                ctx.writeAndFlush(httpResponse);
                            }
                        } catch (Exception e) {
                            log.error("#ConsumerProcessor! onException请求回写失败，request:{}", e.getMessage());
                        }
                    } else {
                        log.error("#ConsumerProcessor! onException event is empty errorMessage:{}", t.getMessage());
                    }
                }
            }
        }
    }
}
