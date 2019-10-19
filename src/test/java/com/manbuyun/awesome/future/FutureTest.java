package com.manbuyun.awesome.future;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.manbuyun.awesome.common.Sleeper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author jinhai
 * @date 2019/03/24
 */
@Slf4j
public class FutureTest {

    @Test
    public void futureTest1() {
        ListenableFuture<String> future = getFuture1();
        Sleeper.sleepForQuietly(10, TimeUnit.SECONDS);

        try {
            // CancellationException + ExecutionException + InterruptedException
            future.get();
        } catch (Exception e) {
            log.error("error", e);
        }
    }

    @Test
    public void futureTest2() {
        CompletableFuture<String> future = getFuture2();
        Sleeper.sleepForQuietly(10, TimeUnit.SECONDS);

        future.whenComplete((result, error) -> {
            // success
            // error
        });
    }

    /**
     * 添加callback+超时cancel功能
     * 超时不是通过future.get()来阻塞实现，而是添加scheduler线程delay超时调起
     * 因为future.get()是被动触发，超时应该是主动监控
     * 当发生超时，FutureCallback的onFailure方法被调起，可以执行资源回收操作
     *
     * @return
     */
    private ListenableFuture<String> getFuture1() {
        ListeningExecutorService executors = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(3));

        ListenableFuture<String> result = executors.submit(() -> {
            Sleeper.sleepForQuietly(5, TimeUnit.SECONDS);
            return "hi";
        });

        Futures.addCallback(result, new FutureCallback<String>() {
            @Override
            public void onSuccess(String result) {
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

    /**
     * 直接返回CompletableFuture. onSuccess、onFailure直接通过whenComplete方法调用
     * jdk9的CompletableFuture有timeout方法支持
     *
     * @return
     */
    private CompletableFuture<String> getFuture2() {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            Sleeper.sleepForQuietly(5, TimeUnit.SECONDS);
            return "hi";
        }, Executors.newSingleThreadExecutor());

//      orTimeout
//      completeOnTimeout

        return future;
    }
}