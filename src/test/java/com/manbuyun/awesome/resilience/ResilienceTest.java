package com.manbuyun.awesome.resilience;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.junit.jupiter.api.Test;

import java.time.Duration;

/**
 * @author jinhai
 * @date 2019/03/04
 */
public class ResilienceTest {

    @Test
    public void testRetry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofSeconds(10))
                .build();

        Retry retry = Retry.of("retry", config);

    }
}