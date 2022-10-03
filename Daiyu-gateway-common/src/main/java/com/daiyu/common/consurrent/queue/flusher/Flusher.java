package com.daiyu.common.consurrent.queue.flusher;

public interface Flusher<E> {
    /**
     * 添加元素
     *
     * @param event
     */
    void add(E event);

    /**
     * 添加多个元素
     *
     * @param event
     */
    void add(@SuppressWarnings("unchecked") E... event);

    /**
     * 尝试添加一个元素，如果成功返回true失败返回false
     *
     * @param event
     * @return
     */
    boolean tryAdd(E event);

    /**
     * 尝试添加多个元素，如果成功返回true失败返回false
     *
     * @param event
     * @return
     */
    boolean tryAdd(E... event);

    /**
     * 判断是否关闭
     *
     * @return
     */
    boolean isShutDown();

    /**
     * 启动
     */
    void start();

    /**
     * 停止≈
     */
    void shutDown();
}
