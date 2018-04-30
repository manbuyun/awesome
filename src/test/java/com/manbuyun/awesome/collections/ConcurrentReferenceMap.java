package com.manbuyun.awesome.collections;

import com.manbuyun.awesome.utils.DefaultSleeper;
import com.manbuyun.awesome.utils.Sleeper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

/**
 * User: cs
 * Date: 2018-04-30
 */
@Slf4j
public class ConcurrentReferenceMap {

    private Sleeper sleeper = new DefaultSleeper();

    @Test
    public void weakReference() {
        ConcurrentReferenceHashMap<String, String> map = new ConcurrentReferenceHashMap<>(16, ConcurrentReferenceHashMap.ReferenceType.WEAK);
        map.put("key", "value");
        log.info("map: {}", map);

        System.gc();

        sleeper.sleepForQuietly(3, TimeUnit.SECONDS);
        log.info("map: {}", map);
    }

    @Test
    public void softReference() {
        ConcurrentReferenceHashMap<String, String> map = new ConcurrentReferenceHashMap<>(16, ConcurrentReferenceHashMap.ReferenceType.SOFT);
        map.put("key", "value");
        log.info("map: {}", map);

        System.gc();

        sleeper.sleepForQuietly(3, TimeUnit.SECONDS);
        log.info("map: {}", map);
    }
}
