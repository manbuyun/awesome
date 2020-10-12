package com.manbuyun.awesome.common;

/**
 * @author jinhai
 * @date 2020/10/12
 */
public class StringBuilderHelper {
    // 2k
    private static final int DISCARD_LIMIT = 1024 << 1;
    private static final ThreadLocal<StringBuilder> threadLocal = ThreadLocal.withInitial(() -> new StringBuilder());

    private StringBuilderHelper() {
    }

    public static StringBuilder get() {
        StringBuilder buf = threadLocal.get();

        if (buf.capacity() > DISCARD_LIMIT) {
            buf.setLength(256);
            buf.trimToSize();
        }
        buf.setLength(0);
        return buf;
    }
}
