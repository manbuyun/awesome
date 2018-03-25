package com.manbuyun.awesome.store;

import com.manbuyun.awesome.serialize.KryoSerDe;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.rocksdb.*;

import java.util.Map;

/**
 * User: cs
 * Date: 2018-02-02
 * <p>
 * 关于Options、DBOptions、ColumnFamilyOptions的配置可参考:jstorm@RocksDbOptionsFactory; flink@PredefinedOptions
 */
@Slf4j
public class RocksDBStore<K, V> implements IFileStore<K, V> {

    protected RocksDB db;

    public RocksDBStore(String path) {
        final Options options = new Options()
                .setCompactionStyle(CompactionStyle.LEVEL)
                .setLevelCompactionDynamicLevelBytes(true)
                .setIncreaseParallelism(4)
                .setUseFsync(false)
                .setMaxOpenFiles(-1)
                .setCreateIfMissing(true)
                .setCreateMissingColumnFamilies(true);

        try {
            db = RocksDB.open(options, path);
        } catch (RocksDBException e) {
            options.close();
            throw new StoreException("Failed to open RocksDB", e);
        }
    }

    @Override
    public V get(K key) {
        Validate.notNull(key);

        try {
            byte[] bytes = db.get(KryoSerDe.serialize(key));
            return KryoSerDe.deserialize(bytes);
        } catch (Exception e) {
            log.error("Failed to get value by key: [{}]", key, e);
        }
        return null;
    }

    @Override
    public void put(K key, V value) {
        Validate.notNull(key);
        Validate.notNull(value);

        try {
            db.put(KryoSerDe.serialize(key), KryoSerDe.serialize(value));
        } catch (Exception e) {
            log.error("Failed to put key: [{}], value: [{}]", key, value, e);
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        Validate.notEmpty(m);

        WriteOptions options = new WriteOptions();
        options.setSync(false).setDisableWAL(true);

        WriteBatch batch = new WriteBatch();
        try {
            for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
                batch.put(KryoSerDe.serialize(entry.getKey()), KryoSerDe.serialize(entry.getValue()));
            }
            db.write(options, batch);
        } catch (Exception e) {
            options.close();
            batch.close();
            log.error("Failed to putAll map: [{}]", m, e);
        }
    }

    @Override
    public void remove(K key) {
        Validate.notNull(key);

        try {
            db.delete(KryoSerDe.serialize(key));
        } catch (Exception e) {
            log.error("Failed to remove key: [{}]", key, e);
        }
    }

    @Override
    public void close() {
        if (db != null) {
            db.close();
        }
    }
}
