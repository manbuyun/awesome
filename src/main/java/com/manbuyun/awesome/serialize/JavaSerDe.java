package com.manbuyun.awesome.serialize;

import java.io.*;
import java.util.Objects;

/**
 * User: cs
 * Date: 2018-04-24
 */
public class JavaSerDe {

    public static <T extends Serializable> byte[] serialize(T t) {
        Objects.requireNonNull(t);

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
            ObjectOutputStream objOut = new ObjectOutputStream(bos);

            objOut.writeObject(t);
            objOut.close();
            return bos.toByteArray();
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to serialize object", e);
        }
    }

    public static <T extends Serializable> T deserialize(byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream objIn = new ObjectInputStream(bis);

            objIn.close();
            return (T) objIn.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalArgumentException("Failed to deserialize object", e);
        }
    }
}
