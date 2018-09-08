package com.task.rest.utils.concurrency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */
public class ConcurrentCache<K, V> {
    private final Map<K, ReferenceHolder<K, V>> map = new ConcurrentHashMap<>();
    private final Supplier<V> factory;
    private final Logger logger = LoggerFactory.getLogger(ConcurrentCache.class);

    public ConcurrentCache(Supplier<V> factory) {
        this.factory = factory;
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        ScheduledFuture<?> future = service.scheduleWithFixedDelay(this::cleanUp, 0, 1, TimeUnit.MINUTES);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            future.cancel(true);
            service.shutdown();
        }));
    }

    private void cleanUp() {
        try {
            Iterator<Map.Entry<K, ReferenceHolder<K, V>>> iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<K, ReferenceHolder<K, V>> entry = iterator.next();
                ReferenceHolder<K, V> holder = entry.getValue();
                if (holder.get() == null) {
                    holder.lock();
                    try {
                        if (holder.get() == null) {
                            iterator.remove();
                        }
                    } finally {
                        holder.unlock();
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Unexpected exception during cleaning up cache:", e);
        }
    }

    public V get(K key) {
        ReferenceHolder<K, V> holder = map.computeIfAbsent(key, k -> new ReferenceHolder<>(key, factory));
        holder.lock();
        try {
            return holder.getOrCreate();
        } finally {
            holder.unlock();
        }

    }


    private static class ReferenceHolder<K, V> {

        private final Lock lock = new ReentrantLock();
        private final Supplier<V> factory;
        private WeakReference<V> weakReference;

        public ReferenceHolder(K key, Supplier<V> factory) {
            this.weakReference = new WeakReference<>(factory.get());
            this.key = key;
            this.factory = factory;
        }

        private final K key;

        public K getKey() {
            return key;
        }

        public void lock() {
            lock.lock();
        }

        public void unlock() {
            lock.unlock();
        }

        public V get() {
            return weakReference.get();
        }

        public V getOrCreate() {
            V value = weakReference.get();
            if (value != null) {
                return value;
            }
            V newValue = factory.get();
            weakReference = new WeakReference<>(newValue);
            return weakReference.get();
        }
    }

}
