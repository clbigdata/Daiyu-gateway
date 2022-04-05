package com.daiyu.gateway.core;

/**
 * @author ：chenlong
 * @filename LifeCycle
 * @date 2022-04-05 10:52
 * @description：生命周期管理接口
 */
public interface LifeCycle {
    /**
     * 生命周期组件初始化
     */
    void init();

    /**
     * 生命周期组件启动
     */
    void start();

    /**
     * 生命周期组件关闭
     */
    void shutdown();
}
