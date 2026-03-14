package com.stockanalyzer.util;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight, zero-dependency TTL cache backed by ConcurrentHashMap.
 *
 * Usage:
 * <pre>
 *     TtlCache&lt;MyValue&gt; cache = new TtlCache&lt;&gt;(Duration.ofMinutes(5));
 *     cache.put("key", value);
 *     MyValue v = cache.get("key"); // null if expired or absent
 * </pre>
 *
 * Thread-safe via ConcurrentHashMap.
 * Expired entries are lazily evicted on access.
 *
 * @param <V> the value type
 */
public class TtlCache<V> {

    private final long ttlMillis;

    private final ConcurrentHashMap<String, Entry<V>> store = new ConcurrentHashMap<>();

    public TtlCache(long ttlMillis) {
        this.ttlMillis = ttlMillis;
    }

    /** Store a value under {@code key}. Resets the TTL for that key. */
    public void put(String key, V value) {
        store.put(key, new Entry<>(value, System.currentTimeMillis() + ttlMillis));
    }

    /**
     * Return the value for {@code key}, or {@code null} if the entry is
     * absent or has expired (expired entries are removed from the map).
     */
    public V get(String key) {
        Entry<V> e = store.get(key);
        if (e == null) return null;
        if (System.currentTimeMillis() > e.expiresAt) {
            store.remove(key);
            return null;
        }
        return e.value;
    }

    /** Remove all entries regardless of TTL. */
    public void invalidateAll() {
        store.clear();
    }

    public int size() {
        return store.size();
    }

    // ── Inner entry ───────────────────────────────────────────────────────

    private static class Entry<V> {
        final V    value;
        final long expiresAt; // epoch millis

        Entry(V value, long expiresAt) {
            this.value     = value;
            this.expiresAt = expiresAt;
        }
    }
}
