/*
 *  Copyright 2011 Mathieu ANCELIN
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package com.mancel01.thetreeof.util;

import com.mancel01.thetreeof.util.C.EnhancedList;
import com.mancel01.thetreeof.util.F.Option;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service registry for very basic dynamic dependency injection.
 * 
 * @author Mathieu ANCELIN
 */
public class Registry {

    private static final ConcurrentHashMap<Key, Map<String, Bean>> beans 
            = new ConcurrentHashMap<Key, Map<String, Bean>>();

    private static final Map<String, String> emptyProps = Collections.emptyMap();
    
    public static void status() {
        SimpleLogger.trace("");
        SimpleLogger.trace("Beans : \n");
        for (Key key : beans.keySet()) {
            SimpleLogger.trace("{} => {}", key, beans.get(key));
            SimpleLogger.trace("");
        }
        SimpleLogger.trace("Listeners : \n");
        for (Class c : EventBus.listeners.keySet()) {
            SimpleLogger.trace("for {}", c);
            for (BeanListener list : EventBus.listeners.get(c)) {
                SimpleLogger.trace("    {}", list.toString());
            }
            SimpleLogger.trace("");
        }
    }
    
    public static <T, U extends T> BeanRegistration<T> register(Class<T> clazz, U implementation) {
        String id = UUID.randomUUID().toString();
        Key key = new Key(clazz, emptyProps);
        Bean<T> bean = new Bean<T>(clazz, implementation, id, emptyProps);
        if (!beans.containsKey(key)) {
            beans.put(key, new HashMap<String, Bean>());
        }
        Map<String, Bean> beanList = beans.get(key);
        if (!beanList.containsKey(id)) {
            beanList.put(id, bean);
        }
        EventBus.processEvent(new BeanEvent(BeanEventType.BEAN_REGISTRATION, bean.reference()));
        return new BeanRegistration<T>(clazz, emptyProps, id);
    }
    
    public static <T, U extends T> BeanRegistration<T> register(final Class<T> clazz, final Class<U> implementation) {
        return register(clazz, new Provider<T>() {
            @Override
            public T get() {
                Object impl = null;
                try {
                    impl = implementation.newInstance();
                } catch (Exception ex) {
                    throw new F.ExceptionWrapper(ex);
                }
                return (T) impl;
            }
        });
    }
    
    public static <T, U extends T> BeanRegistration<T> register(Class<T> clazz, Provider<U> implementation) {
        String id = UUID.randomUUID().toString();
        Key key = new Key(clazz, emptyProps);
        Bean<T> bean = new Bean<T>(clazz, implementation, id, emptyProps);
        if (!beans.containsKey(key)) {
            beans.put(key, new HashMap<String, Bean>());
        }
        Map<String, Bean> beanList = beans.get(key);
        if (!beanList.containsKey(id)) {
            beanList.put(id, bean);
        }
        EventBus.processEvent(new BeanEvent(BeanEventType.BEAN_REGISTRATION, bean.reference()));
        return new BeanRegistration<T>(clazz, emptyProps, id);
    }
    
    public static <T> Option<BeanReference<T>> reference(Class<T> clazz) {
        Key key = new Key(clazz, emptyProps);
        if (!beans.containsKey(key)) {
            return Option.none();
        }
        for (Bean b : beans.get(key).values()) {
            return Option.maybe((BeanReference<T>) b.reference());
        }
        return Option.none();
    }

    public static <T> EnhancedList<BeanReference<?>> references() {
        EnhancedList<BeanReference<?>> refs = C.eList(new ArrayList<BeanReference<?>>());
        for (Map<String, Bean> vals : beans.values()) {
            for (Bean b : vals.values()) {
                refs.add(b.reference());
            }
        }
        return refs;
    }
    
    public static <T> EnhancedList<BeanReference<T>> references(Class<T> clazz) {
        Key key = new Key(clazz, emptyProps);
        if (!beans.containsKey(key)) {
            return C.eList();
        }
        EnhancedList<BeanReference<T>> implems = C.eList(new ArrayList<BeanReference<T>>());
        for (Bean b : beans.get(key).values()) {
            implems.add(b.reference());
        }
        return implems;
    }
    
    public static <T> MaybeReference<T> optionalReference(final Class<T> clazz) {
        return new MaybeReference<T>() {
            @Override
            public Class<T> type() {
                return clazz;
            }

            @Override
            public Option<T> optional() {
                Key key = new Key(clazz, emptyProps);
                if (!beans.containsKey(key)) {
                    return Option.none();
                }
                for (Bean b : beans.get(key).values()) {
                    return b.optional();
                }
                return Option.none();
            }
        };
    }

    public static <T> T instance(Class<T> clazz) {
        Key key = new Key(clazz, emptyProps);
        if (!beans.containsKey(key)) {
            return null;
        }
        for (Bean b : beans.get(key).values()) {
            return (T) b.instance();
        }
        return null;
    }
    
    public static <T> EnhancedList<T> instances() {
        EnhancedList<T> implems = C.eList(new ArrayList<T>());
        for (Map<String, Bean> vals : beans.values()) {
            for (Bean b : vals.values()) {
                implems.add((T) b.instance());
            }
        }
        return implems;
    }

    public static <T> EnhancedList<T> instances(Class<T> clazz) {
        Key key = new Key(clazz, emptyProps);
        if (!beans.containsKey(key)) {
            return C.eList();
        }
        EnhancedList<T> implems = C.eList(new ArrayList<T>());
        for (Bean b : beans.get(key).values()) {
            implems.add((T) b.instance());
        }
        return implems;
    }

    public static BeanListenerRegistration registerListener(BeanListener<?> listener) {
        if (!EventBus.listeners.containsKey(FakeFilterType.class)) {
            EventBus.listeners.putIfAbsent(FakeFilterType.class, new ArrayList<BeanListener<?>>());
        }
        EventBus.listeners.get(FakeFilterType.class).add(listener);
        return new BeanListenerRegistration(listener);
    }

    public static <T> BeanListenerRegistration registerListener(BeanListener<T> listener, Class<T> clazz) {
        if (!EventBus.listeners.containsKey(clazz)) {
            EventBus.listeners.putIfAbsent(clazz, new ArrayList<BeanListener<?>>());
        }
        EventBus.listeners.get(clazz).add(listener);
        return new BeanListenerRegistration(listener);
    }
    
    private static class FilledProvider<T> implements Provider<T> {
        
        private final T impl;

        private FilledProvider(T impl) {
            this.impl = impl;
        }

        @Override
        public T get() {
            return impl;
        }
    }

    private static class Bean<T> {

        private final Class<T> clazz;
        private final Provider<T> implementation;
        private final String id;
        private final Map<String, String> properties;

        public Bean(Class<T> clazz, Object implementation, String id, Map<String, String> properties) {
            this.clazz = clazz;
            this.implementation = new FilledProvider<T>((T) implementation);
            this.id = id;
            this.properties = properties;
        }
        
        public Bean(Class<T> clazz, Provider<T> implementation, String id, Map<String, String> properties) {
            this.clazz = clazz;
            this.implementation = implementation;
            this.id = id;
            this.properties = properties;
        }

        public BeanReference<T> reference() {
            return new BeanReference<T>(clazz, properties, id);
        }

        public T instance() {
            return implementation.get();
        }
        
        public Option<T> optional() {
            return Option.maybe(instance());
        }

        @Override
        public String toString() {
            return "Bean [ " + clazz.getName() + ", impl=" + implementation.toString() + " ]";
        }
    }

    public static class BeanListenerRegistration {

        private final BeanListener listener;
        
        public BeanListenerRegistration(BeanListener listener) {
            this.listener = listener;
        }
        
        public void unregistrer() {
            List<BeanListener<?>> listeners = null;
            for (Class c : EventBus.listeners.keySet()) {
                for (BeanListener list : EventBus.listeners.get(c)) {
                    if (list.equals(listener)) {
                        listeners = EventBus.listeners.get(c);
                    }
                }
            }
            if (listeners != null) {
                listeners.remove(listener);
            }
        }
    }

    private static class Key {

        private final Class<?> clazz;
        private final Map<String, String> properties;

        private Key(Class<?> clazz, Map<String, String> properties) {
            this.clazz = clazz;
            this.properties = properties;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Key other = (Key) obj;
            if (this.clazz != other.clazz && (this.clazz == null || !this.clazz.equals(other.clazz))) {
                return false;
            }
            if (this.properties != other.properties && (this.properties == null || !this.properties.equals(other.properties))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 47 * hash + (this.clazz != null ? this.clazz.hashCode() : 0);
            hash = 47 * hash + (this.properties != null ? this.properties.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return "Key [ " + hashCode() + " ]";
        }
    }

    public static class BeanRegistration<T> {

        private final String id;
        private final Class<T> clazz;
        private final Map<String, String> props;

        private BeanRegistration(Class<T> clazz, Map<String, String> props, String id) {
            this.id = id;
            this.props = props;
            this.clazz = clazz;
        }

        public void unregister() {
            Key key = new Key(clazz, emptyProps);
            if (beans.containsKey(key)) {
                Map<String, Bean> beanList = beans.get(key);
                if (beanList.containsKey(id)) {
                    EventBus.processEvent(new BeanEvent(BeanEventType.BEAN_UNREGISTRATION, reference()));
                    beanList.remove(id);
                }
            }
        }

        public BeanReference<T> reference() {
            Key key = new Key(clazz, emptyProps);
            if (beans.containsKey(key)) {
                Map<String, Bean> beanList = beans.get(key);
                if (beanList.containsKey(id)) {
                    return beanList.get(id).reference();
                }
            }
            return null;
        }

        public T instance() {
            Key key = new Key(clazz, emptyProps);
            if (beans.containsKey(key)) {
                Map<String, Bean> beanList = beans.get(key);
                if (beanList.containsKey(id)) {
                    return (T) beanList.get(id).instance();
                }
            }
            return null;
        }
        
        public Option<T> optional() {
            return Option.maybe(instance());
        }
    }

    private static class EventBus {

        private static ConcurrentHashMap<Class<?>, List<BeanListener<?>>> listeners =
                new ConcurrentHashMap<Class<?>, List<BeanListener<?>>>();

        private static void processEvent(BeanEvent evt) {
            for (Class c : listeners.keySet()) {
                if (c.equals(FakeFilterType.class)) {
                    for (BeanListener listener : listeners.get(c)) {
                        listener.onEvent(evt);
                    }
                } else {
                    if (c.equals(evt.ref().type())) {
                        for (BeanListener listener : listeners.get(c)) {
                            listener.onEvent(evt);
                        }
                    }
                }
            }
        }
    }

    private static class FakeFilterType {}

    public static enum BeanEventType {

        BEAN_REGISTRATION, BEAN_UNREGISTRATION
    }

    public static interface BeanListener<T> {

        public void onEvent(BeanEvent event);
    } 
    
    public static interface Provider<T> {
        T get();
    }
    
    public static interface MaybeReference<T> {
        public Class<T> type();
        public Option<T> optional();
    }

    public static class BeanEvent {

        private final BeanEventType type;
        private final BeanReference<?> ref;

        private BeanEvent(BeanEventType type, BeanReference<?> ref) {
            this.type = type;
            this.ref = ref;
        }

        public BeanEventType type() {
            return type;
        }

        public BeanReference<?> ref() {
            return ref;
        }
    }

    public static class BeanReference<T> implements MaybeReference<T> {

        private final String id;
        private final Class<T> clazz;
        private final Map<String, String> props;

        private BeanReference(Class<T> clazz, Map<String, String> props, String id) {
            this.id = id;
            this.props = props;
            this.clazz = clazz;
        }

        @Override
        public Class<T> type() {
            return clazz;
        }

        public Map<String, String> props() {
            return props;
        }

        public T instance() {
            Key key = new Key(clazz, emptyProps);
            if (beans.containsKey(key)) {
                Map<String, Bean> beanList = beans.get(key);
                if (beanList.containsKey(id)) {
                    return (T) beanList.get(id).instance();
                }
            }
            return null;
        }
        
        @Override
        public Option<T> optional() {
            return Option.maybe(instance());
        }
    }

    /**public static <T> Iterable<BeanReference<?>> references(Map<String, String> properties) {
        throw new UnsupportedOperationException("Need to be implemented");
    }

    public static <T> BeanReference<T> reference(Class<T> clazz, Map<String, String> properties) {
        throw new UnsupportedOperationException("Need to be implemented");
    }

    public static <T> Iterable<BeanReference<T>> references(Class<T> clazz, Map<String, String> properties) {
        throw new UnsupportedOperationException("Need to be implemented");
    }

    public static <T> Iterable<T> instances(Class<T> clazz, Map<String, String> properties) {
        throw new UnsupportedOperationException("Need to be implemented");
    }

    public static <T> T instance(Class<T> clazz, Map<String, String> properties) {
        throw new UnsupportedOperationException("Need to be implemented");
    }

    public static <T, U extends T> BeanRegistration<T> register(Class<T> clazz, U implementation, Map<String, String> properties) {
        throw new UnsupportedOperationException("Need to be implemented");
    }
     
    public static void registerListener(BeanListener<?> listener, Map<String, String> properties) {
        throw new UnsupportedOperationException("Need to be implemented");
    }

    public static <T> void registerListener(BeanListener<T> listener, Class<T> clazz, Map<String, String> properties) {
        throw new UnsupportedOperationException("Need to be implemented");
    }**/
}
