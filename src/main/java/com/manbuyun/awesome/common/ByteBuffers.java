package com.manbuyun.awesome.common;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * User: cs
 * Date: 2018-05-02
 */
public class ByteBuffers {

    public static ByteBuffer toBuffer(byte[] data) {
        return ByteBuffer.wrap(data);
    }

    public static byte[] toBytes(ByteBuffer buffer) {
        int length = buffer.remaining();
        if (buffer.hasArray()) {
            int baseOffset = buffer.arrayOffset() + buffer.position();
            // The length of the returned array will be to - from
            return Arrays.copyOfRange(buffer.array(), baseOffset, baseOffset + length);
        }
        byte[] bytes = new byte[length];
        buffer.duplicate().get(bytes);
        return bytes;
    }
}