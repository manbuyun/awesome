package com.manbuyun.awesome.store;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.commons.lang3.Validate;

/**
 * User: cs
 * Date: 2018-02-05
 */
public class FileStoreFactory {

    private static Cache<String, IFileStore> cache = Caffeine.newBuilder().maximumSize(3).build();

    public static <K, V> IFileStore<K, V> open(String path) {
        Validate.notEmpty(path);

        return cache.get(path, p -> new RocksDBStore<>(p));
    }
}
