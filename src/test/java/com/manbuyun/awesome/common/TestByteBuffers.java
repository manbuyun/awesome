package com.manbuyun.awesome.common;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * User: cs
 * Date: 2018-05-02
 */
public class TestByteBuffers {

    @Test
    public void testToByteBuffer() {
        byte[] b = "Hello_World".getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.put(b);
        buffer.flip();

        byte[] b1 = ByteBuffers.toBytes(buffer);
        assert "Hello_World".equals(new String(b1, StandardCharsets.UTF_8));
    }
}
