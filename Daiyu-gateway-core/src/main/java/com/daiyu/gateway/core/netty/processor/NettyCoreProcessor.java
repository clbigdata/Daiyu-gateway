package com.daiyu.gateway.core.netty.processor;

import com.daiyu.gateway.core.context.HttpRequestWrapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * @author ：chenlong
 * @filename NettyCoreProcessor
 * @date 2022-04-10 10:12
 * @description：核心流程的主执行逻辑
 */
public class NettyCoreProcessor implements NettyProcessor {

    @Override
    public void process(HttpRequestWrapper event) {
        ChannelHandlerContext ctx = event.getCtx();
        FullHttpRequest fullHttpRequest = event.getFullHttpRequest();
        try {
            //1。解析FullHttpRequest，把他们转换为我们自己想要的内部对象：Context
            System.err.println("接收到http请求");
        } catch (Throwable t) {

        }
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }
}
