package com.daiyu.gateway.core.helper;

import com.daiyu.common.enums.ResponseCode;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

/**
 * @author ：chenlong
 * @filename ResponseHelper
 * @date 2022-10-03 19:43
 * @description：
 */
public class ResponseHelper {
    public static FullHttpResponse getHttpResponse(ResponseCode responseCode) {
        String errorContent = "响应内部错误";
        DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.INTERNAL_SERVER_ERROR, Unpooled.wrappedBuffer("响应内部错误".getBytes()));

        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON + ";charset=utf-8");
        httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, errorContent.length());
        return httpResponse;
    }
}
