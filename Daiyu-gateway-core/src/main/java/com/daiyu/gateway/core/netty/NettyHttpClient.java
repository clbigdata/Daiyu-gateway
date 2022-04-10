package com.daiyu.gateway.core.netty;

import com.daiyu.gateway.core.DaiyuGatewayConfig;
import com.daiyu.gateway.core.LifeCycle;
import com.daiyu.gateway.core.helper.AsyncHttpHelper;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.EventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;

/**
 * @author ：chenlong
 * @filename NettyHttpClient
 * @date 2022-04-05 10:59
 * @description：Http客户端启动类
 */
@Slf4j
public class NettyHttpClient implements LifeCycle {

    private AsyncHttpClient asyncHttpClient;

    private DaiyuGatewayConfig gatewayConfig;

    private EventLoopGroup eventLoopGroupWork;

    private DefaultAsyncHttpClientConfig.Builder clientBuilder;

    public NettyHttpClient(DaiyuGatewayConfig gatewayConfig, EventLoopGroup eventLoopGroupWork) {
        this.gatewayConfig = gatewayConfig;
        this.eventLoopGroupWork = eventLoopGroupWork;
    }

    @Override
    public void init() {
        this.clientBuilder = new DefaultAsyncHttpClientConfig.Builder()
                .setFollowRedirect(false)
                .setEventLoopGroup(eventLoopGroupWork)
                .setConnectTimeout(gatewayConfig.getHttpConnectTimeout())
                .setRequestTimeout(gatewayConfig.getHttpRequestTimeout())
                .setMaxConnections(gatewayConfig.getHttpMaxConnections())
                .setAllocator(PooledByteBufAllocator.DEFAULT)
                .setCompressionEnforced(true)
                .setMaxRequestRetry(gatewayConfig.getHttpMaxRequestRetry())
                .setPooledConnectionIdleTimeout(gatewayConfig.getHttpPooledConnectionIdleTimeout())
                .setMaxConnectionsPerHost(1000);
    }

    @Override
    public void start() {
        this.asyncHttpClient = new DefaultAsyncHttpClient(clientBuilder.build());
        AsyncHttpHelper.getInstance().initialized(asyncHttpClient);
        init();
    }

    @Override
    public void shutdown() {
        if (asyncHttpClient != null) {
            try {
                asyncHttpClient.close();
            } catch (Exception e) {
                log.error("#NettyHttpClient.shutdown# shutdown error", e);
            }
        }
    }
}
