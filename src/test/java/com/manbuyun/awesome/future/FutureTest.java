package com.manbuyun.awesome.future;

import com.google.common.util.concurrent.*;
import com.manbuyun.awesome.common.Sleeper;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author jinhai
 * @date 2019/03/24
 */
@Slf4j
public class FutureTest {

    @Test
    public void futureTest() {
        ListenableFuture<String> future = getFuture();
        Sleeper.sleepForQuietly(10, TimeUnit.SECONDS);

        try {
            // CancellationException + ExecutionException + InterruptedException
            future.get();
        } catch (Exception e) {
            log.error("error", e);
        }
    }

    /**
     * 添加callback+超时cancel功能
     * 超时不是通过future.get()来阻塞实现，而是添加scheduler线程delay超时调起
     * 因为future.get()是被动触发，超时应该是主动监控
     * 当发生超时，FutureCallback的onFailure方法被调起，可以执行资源回收操作
     *
     * @return
     */
    private ListenableFuture<String> getFuture() {
        ListeningExecutorService executors = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(3));

        ListenableFuture<String> result = executors.submit(() -> {
            Sleeper.sleepForQuietly(5, TimeUnit.SECONDS);
            return "hi";
        });

        Futures.addCallback(result, new FutureCallback<String>() {
            @Override
            public void onSuccess(@NullableDecl String result) {
                log.info("success");
            }

            @Override
            public void onFailure(Throwable t) {
                log.error("error", t);
            }
        }, Executors.newSingleThreadExecutor());

        Futures.withTimeout(result, 3, TimeUnit.SECONDS, Executors.newSingleThreadScheduledExecutor());

        return result;
    }
}