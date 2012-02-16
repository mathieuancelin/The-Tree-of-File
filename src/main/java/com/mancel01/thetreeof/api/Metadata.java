package com.mancel01.thetreeof.api;

public class Metadata<K, V> {
    
    private final K key;
    private final V value;

    public Metadata(K key, V value) {
        assert key != null;
        assert value != null;
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    @Override
    public String toString() {
        return key + ";" + value;
    }
}
