package com.task.rest.utils.concurrency;

import org.hibernate.internal.util.collections.ConcurrentReferenceHashMap;
import org.hibernate.internal.util.collections.ConcurrentReferenceHashMap.ReferenceType;

import java.util.function.Supplier;

/**
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */
public class HibernateConcurrentCache<K, V> implements ConcurrentCache<K, V> {
    private static final int INIT_CAPACITY = 16;
    private final ConcurrentReferenceHashMap<K, V> map = new ConcurrentReferenceHashMap<>(INIT_CAPACITY, ReferenceType.STRONG, ReferenceType.WEAK);
    private final Supplier<V> factory;

    public HibernateConcurrentCache(Supplier<V> factory) {
        this.factory = factory;
    }

    @Override
    public V get(K key) {
        return map.computeIfAbsent(key, k -> factory.get());
    }

}
