package com.task.rest.utils.concurrency;

/**
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */
public interface ConcurrentCache<K, V> {

    V get(K key);

}
