package com.mancel01.thetreeof.util;

import com.mancel01.thetreeof.api.Store;
import com.mancel01.thetreeof.util.F.Option;
import java.util.HashMap;
import java.util.Map;

public class MemoryStore implements Store {
    
    private final Map<String, String> store;

    public MemoryStore(Map<String, String> store) {
        this.store = store;
    }

    public MemoryStore() {
        this.store = new HashMap<String, String>();
    }

    @Override
    public void persist() {
        // Nothing to do
    }

    @Override
    public Option<String> get(String name) {
        return Option.maybe(store.get(name));
    }

    @Override
    public boolean containsKey(String name) {
        return store.containsKey(name);
    }

    @Override
    public void remove(String name) {
        store.remove(name);
    }

    @Override
    public Option<String> set(String name, String value) {
        return Option.maybe(store.put(name, value));
    }
    
}
