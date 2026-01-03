package com.nhmk.agentic_example.infrastructure.cache;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Very small TTL cache for string keys.
 */
public class SimpleCache<V> {
    private final Map<String, Entry<V>> map = new ConcurrentHashMap<>();
    private final long ttlMillis;

    public SimpleCache(long ttlMillis) {
        this.ttlMillis = ttlMillis;
    }

    public V get(String key) {
        Entry<V> e = map.get(key);
        if (e == null) return null;
        if (Instant.now().toEpochMilli() > e.expiresAt) {
            map.remove(key);
            return null;
        }
        return e.value;
    }

    public void put(String key, V value) {
        map.put(key, new Entry<>(value, Instant.now().toEpochMilli() + ttlMillis));
    }

    private static final class Entry<V> {
        final V value;
        final long expiresAt;
        Entry(V value, long expiresAt) { this.value = value; this.expiresAt = expiresAt; }
    }
}
