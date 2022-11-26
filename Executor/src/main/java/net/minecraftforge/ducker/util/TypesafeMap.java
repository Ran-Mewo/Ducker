package net.minecraftforge.ducker.util;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Typed key-value map, similar to the AttributeKey netty stuff. Uses lambda interfaces for get/set.
 * Originally from CPW's modlauncher
 */
public final class TypesafeMap
{
    private final ConcurrentHashMap<Key<Object>, Object> map = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Key<Object>> keys = new ConcurrentHashMap<>();

    public TypesafeMap() {
    }

    public <V> Optional<V> get(Key<V> key) {
        return Optional.ofNullable(key.clz.cast(map.get(key)));
    }

    public <V> V computeIfAbsent(Key<V> key, Function<? super Key<V>, ? extends V> valueFunction) {
        return computeIfAbsent(this.map, key, valueFunction);
    }

    @SuppressWarnings("unchecked")
    private <C1, C2, V> V computeIfAbsent(ConcurrentHashMap<C1, C2> map, Key<V> key, Function<? super Key<V>, ? extends V> valueFunction) {
        return (V) map.computeIfAbsent((C1) key, (Function<? super C1, ? extends C2>) valueFunction);
    }

    private ConcurrentHashMap<String, Key<Object>> getKeyIdentifiers() {
        return keys;
    }

    /**
     * Unique blackboard key
     */
    public static final class Key<T> implements Comparable<Key<T>> {
        private static final AtomicLong idGenerator = new AtomicLong();
        private final String name;
        private final long uniqueId;
        private final Class<T> clz;

        private Key(String name, Class<T> clz) {
            this.clz = clz;
            this.name = name;
            this.uniqueId = idGenerator.getAndIncrement();
        }

        @SuppressWarnings("unchecked")
        public static <V> Key<V> getOrCreate(TypesafeMap owner, String name, Class<? super V> clazz) {
            Key<V> result = (Key<V>) owner.getKeyIdentifiers().computeIfAbsent(name, (n) -> new Key<>(n, (Class<Object>) clazz));
            if (result.clz != clazz) {
                throw new IllegalArgumentException("Invalid type");
            }
            return result;
        }

        public static <V> Supplier<Key<V>> getOrCreate(Supplier<TypesafeMap> owner, String name, Class<V> clazz) {
            return () -> getOrCreate(owner.get(), name, clazz);
        }

        public final String name() {
            return name;
        }

        @Override
        public int hashCode() {
            return (int) (this.uniqueId ^ (this.uniqueId >>> 32));
        }

        @Override
        public boolean equals(Object obj) {
            try {
                return this.uniqueId == ((Key) obj).uniqueId;
            } catch (ClassCastException cc) {
                return false;
            }
        }

        @Override
        public int compareTo(Key o) {
            if (this == o) {
                return 0;
            }

            if (this.uniqueId < o.uniqueId) {
                return -1;
            }

            if (this.uniqueId > o.uniqueId) {
                return 1;
            }

            throw new RuntimeException("Huh?");
        }
    }
}
