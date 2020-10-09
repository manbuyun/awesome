package com.manbuyun.awesome.serialize;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoPool;
import org.apache.commons.lang3.Validate;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * User: cs
 * Date: 2018-02-01
 */
public class KryoSerDe {

    private static KryoPool pool = new KryoPool.Builder(() -> {
        Kryo kryo = new Kryo();

        // 关闭循环引用，节约空间。但对象有循环嵌套时，可能会出现StackOverflowError
        kryo.setReferences(false);
        kryo.setRegistrationRequired(false);
        kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
        return kryo;
    }).softReferences().build();

    /**
     * 使用writeObject只序列化对象，不记录类信息。反序列时readObject+Class
     * 使用writeClassAndObject时，序列对象和类信息。反序列化readClassAndObject，不需要Class
     *
     * @param t
     * @param <T>
     * @return
     */
    public static <T> byte[] serialize(T t) {
        Validate.notNull(t);

        // apache common-io, not java.io
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Output output = new Output(stream);

        pool.run(kryo -> {
            kryo.writeClassAndObject(output, t);
            return output;
        });
        output.close();

        return stream.toByteArray();
    }

    public static <T> T deserialize(byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        Input input = new Input(new ByteArrayInputStream(bytes));
        T t = (T) pool.run(kryo -> kryo.readClassAndObject(input));
        input.close();

        return t;
    }
}
