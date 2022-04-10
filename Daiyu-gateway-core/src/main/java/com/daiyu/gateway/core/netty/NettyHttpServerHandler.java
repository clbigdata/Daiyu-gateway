package com.daiyu.gateway.core.netty;

import com.daiyu.gateway.core.context.HttpRequestWrapper;
import com.daiyu.gateway.core.netty.processor.NettyProcessor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ：chenlong
 * @filename NettyHttpServerHandler
 * @date 2022-04-05 20:48
 * @description：netty核心处理器
 */
@Slf4j
public class NettyHttpServerHandler extends ChannelInboundHandlerAdapter {
    private NettyProcessor nettyProcessor;

    public NettyHttpServerHandler(NettyProcessor nettyProcessor) {
        this.nettyProcessor = nettyProcessor;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;
            HttpRequestWrapper httpRequestWrapper = new HttpRequestWrapper();
            httpRequestWrapper.setFullHttpRequest(request);
            httpRequestWrapper.setCtx(ctx);
            nettyProcessor.process(httpRequestWrapper);
       } else {
            log.error("#NettpHttpServer.channelRead# Message type is not HttpRequest:{}", msg);
            boolean release = ReferenceCountUtil.release(msg);
            if (!release) {
                log.error("#NettpHttpServer.channelRead# release fail 资源释放失败");
            }
        }
    }

}
