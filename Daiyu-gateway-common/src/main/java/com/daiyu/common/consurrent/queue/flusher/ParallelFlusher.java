package com.daiyu.common.consurrent.queue.flusher;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.WorkerPool;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author ：chenlong
 * @filename ParallelFlusher
 * @date 2022-10-03 15:06
 * @description：并行的fluser多生产者，多消费者，基于disruptor
 */
public class ParallelFlusher<E> implements Flusher<E> {

    private RingBuffer<Holder> ringBuffer;

    private EventListener<E> eventListener;

    private WorkerPool<Holder> workerPool;

    private ExecutorService executorService;

    private EventTranslatorOneArg<Holder, E> eventTranslator;

    public ParallelFlusher(Builder<E> builder) {
        this.eventTranslator = new HolderEventTranslator();
        this.executorService = Executors.newFixedThreadPool(builder.threads, new ThreadFactoryBuilder().setNameFormat("ParallelFlusher-" + builder.namePreFix + "-pool-%d").build());
        this.eventListener = builder.listener;
        RingBuffer<Holder> ringBuffer = RingBuffer.create(builder.producerType, new HolderEventFactory(), builder.bufferSize, builder.waitStrategy);

        SequenceBarrier sequenceBarrier = ringBuffer.newBarrier();

        WorkHandler<Holder>[] workHandlers = new WorkHandler[builder.threads];
        for (int i = 0; i < workHandlers.length; i++) {
            workHandlers[i] = new HolderWorkHandler();
        }

        WorkerPool<Holder> workerPool = new WorkerPool<>(ringBuffer, sequenceBarrier, new HolderExceptionHandler(), workHandlers);

        ringBuffer.addGatingSequences(workerPool.getWorkerSequences());

        this.workerPool = workerPool;


    }

    private static <E> void process(EventListener<E> listener, Throwable e, E event) {
        listener.onException(e, -1, event);
    }

    private static <E> void process(EventListener<E> listener, Throwable e, E... events) {
        for (E event : events) {
            listener.onException(e, -1, event);
        }
    }

    @Override
    public void add(E event) {
        final RingBuffer<Holder> temp = ringBuffer;
        if (ringBuffer == null) {
            process(this.eventListener, new IllegalStateException("ParallelFlusher is closed"), event);
            return;
        }
        try {
            ringBuffer.publishEvent(this.eventTranslator, event);
        } catch (NullPointerException e) {
            process(this.eventListener, new IllegalStateException("ParallelFlusher is closed"), event);
        }
    }

    @Override
    public void add(E... events) {
        final RingBuffer<Holder> temp = ringBuffer;
        if (ringBuffer == null) {
            process(this.eventListener, new IllegalStateException("ParallelFlusher is closed"), events);
            return;
        }
        try {
            ringBuffer.publishEvents(this.eventTranslator, events);
        } catch (NullPointerException e) {
            process(this.eventListener, new IllegalStateException("ParallelFlusher is closed"), events);
        }
    }

    @Override
    public boolean tryAdd(E event) {
        final RingBuffer<Holder> temp = ringBuffer;
        if (ringBuffer == null) {
            return false;
        }
        try {
            return ringBuffer.tryPublishEvent(this.eventTranslator, event);
        } catch (NullPointerException e) {
            return false;
        }
    }

    @Override
    public boolean tryAdd(E... events) {
        final RingBuffer<Holder> temp = ringBuffer;
        if (ringBuffer == null) {
            return false;
        }
        try {
            return ringBuffer.tryPublishEvents(this.eventTranslator, events);
        } catch (NullPointerException e) {
            return false;
        }
    }

    @Override
    public boolean isShutDown() {
        return ringBuffer != null;
    }

    @Override
    public void start() {
        this.ringBuffer = workerPool.start(executorService);
    }

    @Override
    public void shutDown() {
        RingBuffer<Holder> temp = ringBuffer;
        ringBuffer = null;
        if (temp == null) {
            return;
        }
        if (workerPool != null) {
            workerPool.drainAndHalt();
        }
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    public interface EventListener<E> {
        void onEvent(E event) throws Exception;

        void onException(Throwable throwable, long sequence, E event);
    }

    /**
     * 建造者模式，就是为了设置真是对象的属性，在创建真是对象的时候传过去
     *
     * @param <E>
     */
    public static class Builder<E> {
        private ProducerType producerType = ProducerType.MULTI;
        private int bufferSize = 16 * 1024;
        private int threads = 1;
        private String namePreFix = "";
        private WaitStrategy waitStrategy = new BlockingWaitStrategy();
        //消费者监听
        private EventListener<E> listener;

        public Builder<E> setProducerType(ProducerType producerType) {
            Preconditions.checkNotNull(producerType);
            this.producerType = producerType;
            return this;
        }

        public Builder<E> setBufferSize(int bufferSize) {
            Preconditions.checkArgument(Integer.bitCount(bufferSize) == 1);
            this.bufferSize = bufferSize;
            return this;
        }

        public Builder<E> setThreads(int threads) {
            Preconditions.checkArgument(threads > 0);
            this.threads = threads;
            return this;
        }

        public Builder<E> setNamePreFix(String namePreFix) {
            Preconditions.checkNotNull(namePreFix);
            this.namePreFix = namePreFix;
            return this;
        }

        public Builder<E> setWaitStrategy(WaitStrategy waitStrategy) {
            Preconditions.checkNotNull(waitStrategy);
            this.waitStrategy = waitStrategy;
            return this;
        }

        public Builder<E> setEventListener(EventListener<E> listener) {
            Preconditions.checkNotNull(listener);
            this.listener = listener;
            return this;
        }

        public ParallelFlusher<E> build() {
            return new ParallelFlusher<>(this);
        }
    }

    /**
     * 范型替换成真实对象
     */
    private class Holder {
        private E event;

        private void setValue(E event) {
            this.event = event;
        }

        @Override
        public String toString() {
            return "Holder event = " + event;
        }
    }

    private class HolderEventFactory implements EventFactory<Holder> {

        @Override
        public Holder newInstance() {
            return new Holder();
        }
    }

    private class HolderWorkHandler implements WorkHandler<Holder> {

        @Override
        public void onEvent(Holder event) throws Exception {
            eventListener.onEvent(event.event);
            event.setValue(null);
        }
    }

    private class HolderExceptionHandler implements ExceptionHandler<Holder> {

        @Override
        public void handleEventException(Throwable ex, long sequence, Holder event) {
            Holder holder = event;
            try {
                eventListener.onException(ex, sequence, holder.event);
            } catch (Exception e) {

            } finally {

            }
        }

        @Override
        public void handleOnStartException(Throwable ex) {
            throw new UnsupportedOperationException(ex);
        }

        @Override
        public void handleOnShutdownException(Throwable ex) {
            throw new UnsupportedOperationException(ex);
        }
    }

    private class HolderEventTranslator implements EventTranslatorOneArg<Holder, E> {

        @Override
        public void translateTo(Holder holder, long sequence, E event) {
            holder.setValue(event);
        }
    }
}
