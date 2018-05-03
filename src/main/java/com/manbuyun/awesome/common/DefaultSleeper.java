package com.manbuyun.awesome.common;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * User: cs
 * Date: 2018-04-30
 */
@Slf4j
public class DefaultSleeper implements Sleeper {

    @Override
    public void sleepFor(long time, TimeUnit unit) throws InterruptedException {
        unit.sleep(time);
    }

    @Override
    public void sleepForQuietly(long time, TimeUnit unit) {
        try {
            unit.sleep(time);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Error occurred while sleeping", e);
        }
    }
}