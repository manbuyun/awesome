package com.manbuyun.awesome.utils;

import java.util.concurrent.TimeUnit;

/**
 * User: cs
 * Date: 2018-04-30
 */
public interface Sleeper {

    void sleepFor(long time, TimeUnit unit) throws InterruptedException;

    void sleepForQuietly(long time, TimeUnit unit);

}
