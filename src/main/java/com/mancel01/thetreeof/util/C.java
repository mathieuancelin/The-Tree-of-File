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

import static com.mancel01.thetreeof.util.F.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Utilities for collections (inspired by Google Collections)
 *
 * @author Mathieu ANCELIN
 */
public final class C {

    private C() {}
    
    public static <T> EnhancedList<T> eList() {
        return new EnhancedListImpl<T>();
    }
    
    public static <T> EnhancedList<T> eList(List<T> list) {
        return new EnhancedListImpl<T>(list);
    }
    
    public static <T> EnhancedList<T> eList(T firstItem) {
        List<T> list = new ArrayList<T>();
        list.add(firstItem);
        return new EnhancedListImpl<T>(list);
    }
    
    public static <T> EnhancedList<T> eList(T... firstItem) {
        List<T> list = new ArrayList<T>();
        for (T t : firstItem) {
            list.add(t);
        }
        return new EnhancedListImpl<T>(list);
    }
    
    public static interface EnhancedList<T> extends List<T> {
        <R> EnhancedList<R> map(Function<T, R> f);
        <R> EnhancedList<R> parMap(Function<T, R> f);
        EnhancedList<T> filter(Function<T, Boolean> f);
        EnhancedList<T> filterNot(Function<T, Boolean> f);
        EnhancedList<T> parFilter(Function<T, Boolean> f);
        EnhancedList<T> parFilterNot(Function<T, Boolean> f);
        EnhancedList<T> _(T item);
        EnhancedList<T> __(T item);
        EnhancedList<T> _(Collection<T> items);
        EnhancedList<T> __(Collection<T> item);
        EnhancedList<T> rem(T item);
        EnhancedList<T> rem(int index);
        EnhancedList<T> sort(F2<T, T, Integer> comparator);
        EnhancedList<T> distinct();
        EnhancedList<T> foreach(Function<T, Unit> f);
        EnhancedList<T> parForeach(Function<T, Unit> f);
        EnhancedList<T> drop(int n);
        EnhancedList<T> dropRight(int n);
        <K> Map<K, EnhancedList<T>> groupBy(Function<T, K> f);
        Iterable<EnhancedList<T>> grouped(int n);
        T reduce(F2<T, T, T> f);
        T reduceRight(F2<T, T, T> f);
        T head();
        Option<T> headOption();
        T last();
        Option<T> lastOption();
        EnhancedList<T> tail();
        int count(Function<T, Boolean> f);
        Option<T> find(Function<T, Boolean> f);
        String mkString(String with);
        String mkString(String start, String with, String end);
        String parMkString(String with);
        String parMkString(String start, String with, String end);
        EnhancedList<T> takeLeft(int n);
        EnhancedList<T> takeRight(int n);
        EnhancedList<T> takeWhile(Function<T, Boolean> f);
        List<T> toList();
        boolean nonEmpty();
        boolean notContains(T t);
        boolean notContainsAll(Collection<T> t);
        Tuple<EnhancedList<T>, EnhancedList<T>> partition(Function<T, Boolean> f);
        Tuple<EnhancedList<T>, EnhancedList<T>> splitAt(int n);
        EnhancedList<T> reverse();
        EnhancedList<T> union(List<T> list);
    }
    
    private static class EnhancedListImpl<T> extends ArrayList<T> implements EnhancedList<T> {
        
        EnhancedListImpl() {
            super(new ArrayList<T>());
        }
        
        EnhancedListImpl(List<T> list) {
            super(list);
        }
        
        EnhancedListImpl(Collection<T> list) {
            super(list);
        }
        
        @Override
        public EnhancedList<T> rem(T item) {
            this.remove(item);
            return this;
        }
                
        @Override
        public EnhancedList<T> rem(int index) {
            this.remove(index);
            return this;
        }

        @Override
        public <R> EnhancedList<R> map(Function<T, R> f) {
            return new EnhancedListImpl<R>(forEach(this).apply(f).get());
        }

        @Override
        public <R> EnhancedList<R> parMap(Function<T, R> f) {
            return new EnhancedListImpl<R>(forEach(this).parApply(f).get());
        }

        @Override
        public EnhancedList<T> filter(final Function<T, Boolean> f) {
            return new EnhancedListImpl<T>(forEach(this).filteredBy(new Predicate<T>() {

                @Override
                public Boolean apply(T t) {
                    return f.apply(t);
                }
            }).get());
        }

        @Override
        public EnhancedList<T> filterNot(final Function<T, Boolean> f) {
            return new EnhancedListImpl<T>(forEach(this).filteredBy(new Predicate<T>() {

                @Override
                public Boolean apply(T t) {
                    return !f.apply(t);
                }
            }).get());
        }

        @Override
        public EnhancedList<T> parFilter(final Function<T, Boolean> f) {
            return new EnhancedListImpl<T>(forEach(this).parFilteredBy(new Predicate<T>() {

                @Override
                public Boolean apply(T t) {
                    return f.apply(t);
                }
            }).get());
        }

        @Override
        public EnhancedList<T> parFilterNot(final Function<T, Boolean> f) {
            return new EnhancedListImpl<T>(forEach(this).parFilteredBy(new Predicate<T>() {

                @Override
                public Boolean apply(T t) {
                    return !f.apply(t);
                }
            }).get());
        }

        @Override
        public EnhancedList<T> _(T item) {
            this.add(item);
            return this;
        }

        @Override
        public EnhancedList<T> foreach(final Function<T, Unit> f) {
            return new EnhancedListImpl<T>(forEach(this).execute(new Action<T>() {

                @Override
                public void apply(T t) {
                    f.apply(t);
                }
            }).get());
        }

        @Override
        public EnhancedList<T> parForeach(final Function<T, Unit> f) {
            return new EnhancedListImpl<T>(forEach(this).parExecute(new Action<T>() {

                @Override
                public void apply(T t) {
                    f.apply(t);
                }
            }).get());
        }

        @Override
        public T head() {
            if (this.isEmpty()) {
                return null;
            } else {
                return this.get(0);
            }
        }

        @Override
        public Option<T> headOption() {
            if (this.isEmpty()) {
                return Option.none();
            } else {
                return Option.maybe(this.get(0));
            }
        }

        @Override
        public EnhancedList<T> tail() {
            if (!this.isEmpty()) {
                return new EnhancedListImpl<T>(this.subList(1, this.size()));
            } else {
                return new EnhancedListImpl<T>(new ArrayList<T>());
            }
        }

        @Override
        public int count(Function<T, Boolean> f) {
            int count = 0;
            for (T t : this) {
                if (f.apply(t)) {
                    count ++;
                }
            }
            return count;
        }

        @Override
        public Option<T> find(Function<T, Boolean> f) {
            for (T t : this) {
                if (f.apply(t)) {
                    return Option.maybe(t);
                }
            }
            return Option.none();
        }

        @Override
        public String mkString(String with) {
            return C.join(this).with(with);
        }

        @Override
        public String parMkString(String with) {
            return C.join(this).parWith(with);
        }

        @Override
        public EnhancedList<T> takeLeft(int n) {
            List<T> list = new ArrayList<T>();
            if (!this.isEmpty()) {
                for (int i = 0; i < n; i++) {
                    try {
                        list.add(this.get(i));
                    } catch (Exception e) {}
                }
            }
            return new EnhancedListImpl<T>(list);
        }

        @Override
        public EnhancedList<T> takeRight(int n) {
            List<T> list = new ArrayList<T>();
            if (!this.isEmpty()) {
                for (int i = n; i > 0; i--) {
                    try {
                        list.add(this.get(i + 1));
                    } catch (Exception e) {}
                }
            }
            return new EnhancedListImpl<T>(list);
        }

        @Override
        public EnhancedList<T> takeWhile(Function<T, Boolean> f) {
            List<T> list = new ArrayList<T>();
            for (T t : this) {
                if (f.apply(t)) {
                    list.add(t);
                }
            }
            return new EnhancedListImpl<T>(list);
        }

        @Override
        public T reduce(F2<T, T, T> f) {
            T result = null;
            for (T t : this) {
                if (t != null) {
                    if (result == null) {
                        result = t;
                    } else {
                        result = f.apply(result, t);
                    }
                }
            }
            return result;
        }

        @Override
        public T reduceRight(F2<T, T, T> f) {
            T result = null;
            for (int i = this.size(); i > 0; i--) {
                try {
                    T t = this.get(i - 1);
                    if (t != null) {
                        if (result == null) {
                            result = t;
                        } else {
                            result = f.apply(result, t);
                        }
                    }
                } catch (Exception e) {}
            }
            return result;
        }

        @Override
        public List<T> toList() {
            return (List<T>) this;
        }

        @Override
        public EnhancedList<T> __(T item) {
            this.add(this.size(), item);
            return this;
        }

        @Override
        public EnhancedList<T> _(Collection<T> items) {
            this.addAll(items);
            return this;
        }

        @Override
        public EnhancedList<T> __(Collection<T> item) {
            this.addAll(size(), item);
            return this;
        }

        @Override
        public EnhancedList<T> sort(final F2<T, T, Integer> comparator) {
            Collections.sort(this, new Comparator<T>() {
                @Override
                public int compare(T o1, T o2) {
                    return comparator.apply(o1, o2);
                }
            });
            return this;
        }

        @Override
        public EnhancedList<T> distinct() {
            Set<T> dist = new HashSet<T>(this);
            return new EnhancedListImpl<T>(dist);
        }

        @Override
        public EnhancedList<T> drop(int n) {
            if (!isEmpty()) {
                return new EnhancedListImpl<T>(this.subList(0, n));
            }
            return new EnhancedListImpl<T>(new ArrayList<T>());
        }

        @Override
        public EnhancedList<T> dropRight(int n) {
            if (!isEmpty()) {
                return new EnhancedListImpl<T>(this.subList(0, size() - n));
            }
            return new EnhancedListImpl<T>(new ArrayList<T>());
        }

        @Override
        public <K> Map<K, EnhancedList<T>> groupBy(Function<T, K> f) {
            Map<K, EnhancedList<T>> result = new HashMap<K, EnhancedList<T>>();
            for (T t : this) {
                K k = f.apply(t);
                if (!result.containsKey(k)) {
                    result.put(k, new EnhancedListImpl<T>(new ArrayList<T>()));
                }
                result.get(k).add(t);
            }
            return result;
        }

        @Override
        public Iterable<EnhancedList<T>> grouped(int n) {
            List<List<T>> list = C.paginate(this, n);
            List<EnhancedList<T>> result = new ArrayList<EnhancedList<T>>();
            for (List<T> l : list) {
                result.add(new EnhancedListImpl<T>(l));
            }
            return (Iterable<EnhancedList<T>>) result;
        }

        @Override
        public T last() {
            if (!isEmpty()) {
                return get(size() - 1);
            }
            return null;
        }

        @Override
        public Option<T> lastOption() {
            if (!isEmpty()) {
                return Option.maybe(get(size() - 1));
            }
            return Option.none();
        }

        @Override
        public String mkString(String start, String with, String end) {
            return C.join(this).before(start).after(end).with(with);
        }

        @Override
        public String parMkString(String start, String with, String end) {
            return C.join(this).before(start).after(end).parWith(with);
        }

        @Override
        public boolean nonEmpty() {
            return !isEmpty();
        }

        @Override
        public Tuple<EnhancedList<T>, EnhancedList<T>> partition(Function<T, Boolean> f) {
            EnhancedListImpl<T> l1 = new EnhancedListImpl<T>(new ArrayList<T>());
            EnhancedListImpl<T> l2 = new EnhancedListImpl<T>(new ArrayList<T>());
            for (T t : this) {
                if (f.apply(t)) {
                    l1.add(t);
                } else {
                    l2.add(t);
                }
            }
            return new Tuple<EnhancedList<T>, EnhancedList<T>>(l1, l2);
        }

        @Override
        public Tuple<EnhancedList<T>, EnhancedList<T>> splitAt(int n) {
            return new Tuple<EnhancedList<T>, EnhancedList<T>>(
                    new EnhancedListImpl<T>(subList(0, n)), 
                    new EnhancedListImpl<T>(subList(n + 1, size() - 1))
                    );
        }

        @Override
        public EnhancedList<T> reverse() {
            Collections.reverse(this);
            return this;
        }

        @Override
        public EnhancedList<T> union(List<T> list) {
            EnhancedListImpl<T> result = new EnhancedListImpl<T>(this);
            for (T t : list) {
                if (!result.contains(t)) {
                    result.add(t);
                }
            }
            return result;
        }

        @Override
        public boolean notContains(T t) {
            return !contains(t);
        }

        @Override
        public boolean notContainsAll(Collection<T> t) {
            return !containsAll(t);
        }
    } 
    
    /**
     * Return an object capable of applying a function for each item in the collection.
     *
     * @param <T>
     * @param collection the processed collection
     * @return an Each Object
     */
    public static <T> Each<T> forEach(Collection<T> collection) {
        return new EachImpl<T>(collection);
    }

    public static <T> Collection<T> filtered(Collection<T> collection, Predicate<T> predicate) {
        return new EachImpl<T>(collection).filteredBy(predicate).get();
    }

    public static <T> Filterable<T> filter(Collection<T> collection, Predicate<T> predicate) {
        return new EachImpl<T>(collection).filteredBy(predicate);
    }

    public static <T> Joiner join(Collection<T> collection) {
        return new JoinerImpl(collection);
    }

    public static <T> List<List<T>> paginate(List<T> list, int size) {
        List<List<T>> collections = new ArrayList<List<T>>();
        int fromIndex = 0;
        boolean again = true;
        while (again) {
            if (fromIndex > list.size()) {
                break;
            }
            int toIndex = (fromIndex - 1) + size;
            if (toIndex > (list.size() - 1)) {
                toIndex = (list.size() - 1);
                again = false;
            }
            collections.add(list.subList(fromIndex, toIndex));
            fromIndex = toIndex + 1;
        }
        return collections;
        
    }

    public static Predicate<String> eq(final String value) {
        return new Predicate<String>() {
            @Override
            public Boolean apply(String t) {
                return value.equals(t);
            }
        };
    }
    public static Predicate<Integer> eq(final int value) {
        return new Predicate<Integer>() {
            @Override
            public Boolean apply(Integer t) {
                return value == t.intValue();
            }
        };
    }
    public static Predicate<Long> eq(final long value) {
        return new Predicate<Long>() {
            @Override
            public Boolean apply(Long t) {
                return value == t.longValue();
            }
        };
    }
    public static Predicate<Object> eq(final Object value) {
        return new Predicate<Object>() {
            @Override
            public Boolean apply(Object t) {
                return value.equals(t);
            }
        };
    }
    public static Predicate<String> notEq(final String value) {
        return new Predicate<String>() {
            @Override
            public Boolean apply(String t) {
                return !value.equals(t);
            }
        };
    }
    public static Predicate<Long> notEq(final long value) {
        return new Predicate<Long>() {
            @Override
            public Boolean apply(Long t) {
                return value != t.longValue();
            }
        };
    }
    public static Predicate<Integer> notEq(final int value) {
        return new Predicate<Integer>() {
            @Override
            public Boolean apply(Integer t) {
                return value != t.intValue();
            }
        };
    }
    public static Predicate<Object> notEq(final Object value) {
        return new Predicate<Object>() {
            @Override
            public Boolean apply(Object t) {
                return value.equals(t);
            }
        };
    }
    public static Predicate<Object> isNull(final Object value) {
        return new Predicate<Object>() {
            @Override
            public Boolean apply(Object t) {
                return value == null;
            }
        };
    }
    public static Predicate<Object> notNull(final Object value) {
        return new Predicate<Object>() {
            @Override
            public Boolean apply(Object t) {
                return !(value == null);
            }
        };
    }
    public static Predicate<String> matchRegex(final String regexp) {
        return new Predicate<String>() {
            @Override
            public Boolean apply(String t) {
                return regexp.matches(t);
            }
        };
    }
    public static Predicate<Integer> greaterThan(final int value) {
        return new Predicate<Integer>() {
            @Override
            public Boolean apply(Integer t) {
                return value < t.intValue();
            }
        };
    }
    public static Predicate<Long> greaterThan(final long value) {
        return new Predicate<Long>() {
            @Override
            public Boolean apply(Long t) {
                return value < t.longValue();
            }
        };
    }
    public static Predicate<Integer> lesserThan(final int value) {
        return new Predicate<Integer>() {
            @Override
            public Boolean apply(Integer t) {
                return value > t.intValue();
            }
        };
    }
    public static Predicate<Long> lesserThan(final long value) {
        return new Predicate<Long>() {
            @Override
            public Boolean apply(Long t) {
                return value > t.longValue();
            }
        };
    }
    public static Predicate<Integer> greaterEqThan(final int value) {
        return new Predicate<Integer>() {
            @Override
            public Boolean apply(Integer t) {
                return value <= t.intValue();
            }
        };
    }
    public static Predicate<Long> greaterEqThan(final long value) {
        return new Predicate<Long>() {
            @Override
            public Boolean apply(Long t) {
                return value <= t.longValue();
            }
        };
    }
    public static Predicate<Integer> lesserEqThan(final int value) {
        return new Predicate<Integer>() {
            @Override
            public Boolean apply(Integer t) {
                return value >= t.intValue();
            }
        };
    }
    public static Predicate<Long> lesserEqThan(final long value) {
        return new Predicate<Long>() {
            @Override
            public Boolean apply(Long t) {
                return value >= t.longValue();
            }
        };
    }

    public static interface Each<T> extends Filterable<T> {

        @Override
        Each<T> filteredBy(Predicate<T> predicate);

        @Override
        Each<T> parFilteredBy(Predicate<T> predicate);

        Filterable<T> execute(Action<T> action);

        <R> Filterable<R> apply(Function<T, R> transformation);

        Filterable<T> parExecute(Action<T> action);

        <R> Filterable<R> parApply(Function<T, R> transformation);
    }

    public static interface Joiner {

        <T> Joiner labelized(Function<T, String> tranformation);

        Joiner before(String before);

        Joiner after(String after);

        String with(String separator);

        String parWith(String separator);
    }

    public static interface Predicate<T> extends Function<T, Boolean> {

    }

    public static interface Filterable<T> {
        
        Filterable<T> filteredBy(Predicate<T> predicate);

        Filterable<T> parFilteredBy(Predicate<T> predicate);

        Collection<T> get();

        int count();

        boolean isEmpty();

    }

    private static class JoinerImpl implements Action, Joiner {

        private String separator;

        private final StringBuilder builder = new StringBuilder();

        private final Collection<?> value;

        private String before;

        private String after;

        private Function label = new Function<Object, String>() {

            @Override
            public String apply(Object t) {
                return t.toString();
            }
        };

        public JoinerImpl(Collection<?> value) {
            this.value = value;
        }

        @Override
        public void apply(Object t) {
            builder.append(label.apply(t));
            builder.append(separator);
        }

        @Override
        public String with(String separator) {
            this.separator = separator;
            if (before != null) {
                builder.append(before);
            }
            forEach(value).execute((Action) this);
            if (before != null) {
                builder.append(after);
            }
            String finalValue = builder.toString();
            return finalValue.substring(0, finalValue.lastIndexOf(separator))
                    + finalValue.substring(finalValue.lastIndexOf(separator) + separator.length());
        }

        @Override
        public String parWith(String separator) {
            this.separator = separator;
            if (before != null) {
                builder.append(before);
            }
            forEach(value).parExecute((Action) this);
            if (before != null) {
                builder.append(after);
            }
            String finalValue = builder.toString();
            return finalValue.substring(0, finalValue.lastIndexOf(separator))
                    + finalValue.substring(finalValue.lastIndexOf(separator) + separator.length());
        }

        @Override
        public <R> Joiner labelized(Function<R, String> tranformation) {
            this.label = tranformation;
            return this;
        }

        @Override
        public Joiner before(String before) {
            this.before = before;
            return this;
        }

        @Override
        public Joiner after(String after) {
            this.after = after;
            return this;
        }
    }

    private static class EachImpl<T> implements Each<T> {

        private static final int NBR_CORE =
                Runtime.getRuntime().availableProcessors() + 1;

        private final ExecutorService executor = 
            Executors.newFixedThreadPool(NBR_CORE);

        private final Collection<T> baseCollection;
        
        private List<T> workingCollection;

        public EachImpl(Collection<T> baseCollection) {
            this.baseCollection = baseCollection;
             initWorkingCollection();
        }

        @Override
        public Each<T> filteredBy(Predicate<T> predicate) {
            initWorkingCollection();
            Collection<T> tmp = new ArrayList<T>();
            for (T element : workingCollection) {
                if (!predicate.apply(element)) {
                    tmp.add(element);
                }
            }
            for (T element : tmp) {
                workingCollection.remove(element);
            }
            return this;
        }

        private void initWorkingCollection() {
            if (workingCollection == null) {
                workingCollection = new ArrayList<T>();
                for (T element : baseCollection) {
                    workingCollection.add(element);
                }
            }
        }

        @Override
        public Filterable<T> execute(Action<T> action) {
            initWorkingCollection();
            for (T element : workingCollection) {
                action.apply(element);
            }
            return this;
        }

        @Override
        public <R> Filterable<R> apply(Function<T, R> transformation) {
            initWorkingCollection();
            Collection<R> tmp = new ArrayList<R>();
            for (T element : workingCollection) {
                tmp.add(transformation.apply(element));
            }
            return new EachImpl<R>(tmp);
        }


        private Collection<Bound> getBulkBounds() {
            Collection<Bound> bulksCollections = new ArrayList<Bound>();
            int bulkCollectionSize = (workingCollection.size() / NBR_CORE);
            int fromIndex = 0;
            boolean again = true;
            while (again) {
                if (fromIndex > workingCollection.size()) {
                    break;
                }
                int toIndex = (fromIndex - 1) + bulkCollectionSize;
                if (toIndex > (workingCollection.size() - 1)) {
                    toIndex = (workingCollection.size() - 1);
                    again = false;
                }
                bulksCollections.add(new Bound(fromIndex, toIndex));
                fromIndex = toIndex + 1;
            }
            return bulksCollections;
        }

        @Override
        public Filterable<T> parExecute(final Action<T> action) {
            Collection<Future<Void>> bulkExecutions = new ArrayList<Future<Void>>();
            initWorkingCollection();
            Collection<Bound> bulkBounds = getBulkBounds();
            for (Bound bound : bulkBounds) {
                bulkExecutions.add(
                    executor.submit(
                        new BulkExecution<T>(
                            action, workingCollection, bound)));
            }
            for (Future<Void> f : bulkExecutions) {
                try {
                    f.get();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            return this;
        }

        @Override
        public <R> Filterable<R> parApply(final Function<T, R> transformation) {
            Collection<R> tmp = new ArrayList<R>();
            Collection<Future<Collection<R>>> bulkExecutions = new ArrayList<Future<Collection<R>>>();
            initWorkingCollection();
            Collection<Bound> bulkBounds = getBulkBounds();
            for (Bound bound : bulkBounds) {
                bulkExecutions.add(
                    executor.submit(
                        new BulkTransformation<T, R>(
                            transformation, workingCollection, bound)));
            }
            for (Future<Collection<R>> f : bulkExecutions) {
                try {
                    tmp.addAll(f.get());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            return new EachImpl<R>(tmp);
        }

        @Override
        public Each<T> parFilteredBy(Predicate<T> predicate) {
            Collection<T> tmp = new ArrayList<T>();
            Collection<Future<Collection<T>>> bulkExecutions = 
                    new ArrayList<Future<Collection<T>>>();
            initWorkingCollection();
            Collection<Bound> bulkBounds = getBulkBounds();
            for (Bound bound : bulkBounds) {
                bulkExecutions.add(
                    executor.submit(
                        new BulkFilter<T>(
                            predicate, workingCollection, bound)));
            }
            for (Future<Collection<T>> f : bulkExecutions) {
                try {
                    tmp.addAll(f.get());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            for (T element : tmp) {
                workingCollection.remove(element);
            }
            return this;
        }

        @Override
        public Collection<T> get() {
            return workingCollection;
        }

        @Override
        public int count() {
            return workingCollection.size();
        }

        @Override
        public boolean isEmpty() {
            return workingCollection.isEmpty();
        }
    }

    private static class BulkFilter<T> implements Callable<Collection<T>> {

        private final Predicate<T> predicate;
        private final List<T> collection;
        private final Bound bound;

        public BulkFilter(Predicate<T> predicate, List<T> collection, Bound bound) {
            this.predicate = predicate;
            this.collection = collection;
            this.bound = bound;
        }

        @Override
        public Collection<T> call() throws Exception {
            Collection<T> tmp = new ArrayList<T>();
            for (int i = bound.fromIndex; i < (bound.toIndex + 1); i++) {
                T element = collection.get(i);
                if (!predicate.apply(element)) {
                    tmp.add(element);
                }
            }
            return tmp;
        }
    }

    private static class BulkExecution<T> implements Callable<Void> {

        private final Action<T> action;
        private final List<T> collection;
        private final Bound bound;

        public BulkExecution(Action<T> action, List<T> collection, Bound bound) {
            this.action = action;
            this.collection = collection;
            this.bound = bound;
        }

        @Override
        public Void call() throws Exception {
            for (int i = bound.fromIndex; i < (bound.toIndex + 1); i++) {
                action.apply(collection.get(i));
            }
            return null;
        }
    }

    private static class BulkTransformation<T, R> implements Callable<Collection<R>> {

        private final Function<T, R> transfo;
        private final List<T> collection;
        private final Bound bound;

        public BulkTransformation(Function<T, R> transfo, List<T> collection, Bound bound) {
            this.transfo = transfo;
            this.collection = collection;
            this.bound = bound;
        }

        @Override
        public Collection<R> call() throws Exception {
            Collection<R> tmp = new ArrayList<R>();
            for (int i = bound.fromIndex; i < (bound.toIndex + 1); i++) {
                tmp.add(transfo.apply(collection.get(i)));
            }
            return tmp;
        }
    }

    private static class Bound {
        final int fromIndex;
        final int toIndex;

        public Bound(int fromIndex, int toIndex) {
            this.fromIndex = fromIndex;
            this.toIndex = toIndex;
        }

        @Override
        public String toString() {
            return "Bound [ from=" + fromIndex + ", to=" + toIndex + " ]";
        }
    }
}
