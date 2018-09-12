package com.task.rest.utils.concurrency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
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
 * Java Core implementation for the {@link ConcurrentCache}
 *
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 * @see ConcurrentCache
 */
public class CoreJavaConcurrentCache<K, V> implements ConcurrentCache<K, V> {
    private final Map<K, ReferenceHolder<V>> map = new ConcurrentHashMap<>();
    private final Supplier<V> factory;
    private final Logger logger = LoggerFactory.getLogger(CoreJavaConcurrentCache.class);

    @Inject
    public CoreJavaConcurrentCache(Supplier<V> factory) {
        this.factory = factory;
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        ScheduledFuture<?> future = service.scheduleWithFixedDelay(this::cleanUp, 0, 1, TimeUnit.MINUTES);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            future.cancel(true);
            service.shutdown();
        }));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V get(K key) {
        ReferenceHolder<V> holder = map.computeIfAbsent(key, k -> new ReferenceHolder<>(factory));
        holder.lock();
        try {
            return holder.getOrCreate();
        } finally {
            holder.unlock();
        }

    }

    private void cleanUp() {
        try {
            Iterator<Map.Entry<K, ReferenceHolder<V>>> iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<K, ReferenceHolder<V>> entry = iterator.next();
                ReferenceHolder<V> holder = entry.getValue();
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

    private static final class ReferenceHolder<V> {

        private final Lock lock = new ReentrantLock();
        private final Supplier<V> factory;
        private WeakReference<V> weakReference;

        private ReferenceHolder(Supplier<V> factory) {
            this.weakReference = new WeakReference<>(factory.get());
            this.factory = factory;
        }

        private void lock() {
            lock.lock();
        }

        private void unlock() {
            lock.unlock();
        }

        private V get() {
            return weakReference.get();
        }

        private V getOrCreate() {
            V value = weakReference.get();
            if (value != null) {
                return value;
            }
            V newValue = factory.get();
            weakReference = new WeakReference<>(newValue);
            return newValue;
        }
    }

}
