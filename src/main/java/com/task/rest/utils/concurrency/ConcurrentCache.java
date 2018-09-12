package com.task.rest.utils.concurrency;

/**
 * Generic concurrent cache interface
 *
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */
public interface ConcurrentCache<K, V> {

    /**
     * @param key specified key
     * @return value associated with the key
     * or create new default value and puts it to the cache if this cache contains no mapping for the key
     */
    V get(K key);

}
