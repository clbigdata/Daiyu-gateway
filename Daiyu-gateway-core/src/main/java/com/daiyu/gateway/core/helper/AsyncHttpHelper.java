package com.daiyu.gateway.core.helper;

import org.asynchttpclient.AsyncHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;

import java.util.concurrent.CompletableFuture;

/**
 * @author ：chenlong
 * @filename AsyncHttpHelper
 * @date 2022-04-10 12:50
 * @description：异步Http辅助类
 */
public class AsyncHttpHelper {
    private static final class SingletonHelper {
        private static final AsyncHttpHelper INSTANCE = new AsyncHttpHelper();
    }

    public AsyncHttpHelper() {
    }

    public static AsyncHttpHelper getInstance() {
        return SingletonHelper.INSTANCE;
    }

    private AsyncHttpClient asyncHttpClient;

    public void initialized(AsyncHttpClient asyncHttpClient) {
        this.asyncHttpClient = asyncHttpClient;
    }

    public <T>CompletableFuture<T> executeRequest(Request request, AsyncHandler<T> handler) {
        ListenableFuture<T> future = asyncHttpClient.executeRequest(request,handler);
        return future.toCompletableFuture();
    }
}
