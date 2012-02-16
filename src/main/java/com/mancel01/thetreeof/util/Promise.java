package com.mancel01.thetreeof.util;

import com.mancel01.thetreeof.util.F.ExceptionWrapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

public class Promise<V> implements Future<V>, F.Action<V> {

    private final CountDownLatch taskLock = new CountDownLatch(1);
    private boolean cancelled = false;
    private List<F.Action<V>> callbacks = new ArrayList<F.Action<V>>();
    private boolean invoked = false;
    private V result = null;

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return cancelled;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return invoked;
    }

    public V getOrNull() {
        return result;
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        taskLock.await();
        return result;
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        taskLock.await(timeout, unit);
        return result;
    }

    @Override
    public void apply(V result) {
        synchronized (this) {
            if (!invoked) {
                invoked = true;
                this.result = result;
                taskLock.countDown();
            } else {
                return;
            }
        }
        for (F.Action<V> callback : callbacks) {
            callback.apply(result);
        }
    }

    public void onRedeem(F.Action<V> callback) {
        synchronized (this) {
            if (!invoked) {
                callbacks.add(callback);
            }
        }
        if (invoked) {
            callback.apply(result);
        }
    }

    public static <T> Promise<List<T>> waitAll(final Promise<T>... promises) {
        return waitAll(Arrays.asList(promises));
    }

    public static <T> Promise<List<T>> waitAll(final Collection<Promise<T>> promises) {
        final CountDownLatch waitAllLock = new CountDownLatch(promises.size());
        final Promise<List<T>> result = new Promise<List<T>>() {

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                boolean r = true;
                for (Promise<T> f : promises) {
                    r = r & f.cancel(mayInterruptIfRunning);
                }
                return r;
            }

            @Override
            public boolean isCancelled() {
                boolean r = true;
                for (Promise<T> f : promises) {
                    r = r & f.isCancelled();
                }
                return r;
            }

            @Override
            public boolean isDone() {
                boolean r = true;
                for (Promise<T> f : promises) {
                    r = r & f.isDone();
                }
                return r;
            }

            @Override
            public List<T> get() throws InterruptedException, ExecutionException {
                waitAllLock.await();
                List<T> r = new ArrayList<T>();
                for (Promise<T> f : promises) {
                    r.add(f.get());
                }
                return r;
            }

            @Override
            public List<T> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                waitAllLock.await(timeout, unit);
                return get();
            }
        };
        final F.Action<T> action = new F.Action<T>() {

            @Override
            public void apply(T completed) {
                waitAllLock.countDown();
                if (waitAllLock.getCount() == 0) {
                    try {
                        result.apply(result.get());
                    } catch (Exception e) {
                        throw new ExceptionWrapper(e);
                    }
                }
            }
        };
        for (Promise<T> f : promises) {
            f.onRedeem(action);
        }
        return result;
    }

    public static <T> Promise<T> waitAny(final Promise<T>... futures) {
        final Promise<T> result = new Promise<T>();
        final F.Action<T> action = new F.Action<T>() {

            @Override
            public void apply(T completed) {
                synchronized (this) {
                    if (result.isDone()) {
                        return;
                    }
                }
                result.apply(completed);
            }
        };
        for (Promise<T> f : futures) {
            f.onRedeem(action);
        }
        return result;
    }
}