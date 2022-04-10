package com.daiyu.gateway.core.context;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.Data;

/**
 * @author ：chenlong
 * @filename HttpRequestWrapper
 * @date 2022-04-10 10:06
 * @description：请求包装类
 */
@Data
public class HttpRequestWrapper {
    private FullHttpRequest fullHttpRequest;

    private ChannelHandlerContext ctx;
}
