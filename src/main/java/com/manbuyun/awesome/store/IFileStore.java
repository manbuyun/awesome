package com.manbuyun.awesome.store;

import java.util.Map;

/**
 * User: cs
 * Date: 2018-02-01
 */
public interface IFileStore<K, V> {

    V get(K key);

    void put(K key, V value);

    void putAll(Map<? extends K, ? extends V> m);

    void close();
}
