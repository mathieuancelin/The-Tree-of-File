package com.mancel01.thetreeof.task;

import com.mancel01.thetreeof.api.Task;
import com.mancel01.thetreeof.util.SimpleLogger;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class TaskExecutor extends Thread {
    
    private CountDownLatch latch;
    
    private final ConcurrentLinkedQueue<Task> mailbox =
                new ConcurrentLinkedQueue<Task>();
    
    private final AtomicBoolean started = new AtomicBoolean(false);
    
    private final AtomicReference<CountDownLatch> countRef = 
            new AtomicReference<CountDownLatch>();
    
    private static final ExecutorService exec = Executors.newCachedThreadPool();

    public TaskExecutor() {
        latch = new CountDownLatch(1);
        countRef.set(latch);
    }
    
    public static void startTaskExecutor(TaskExecutor executor) {
        exec.submit(executor);
    }
    
    public static void stopTaskExecutor(TaskExecutor executor) {
        executor.stopExecutor();
        exec.shutdown();
        //List<Runnable> runnables = exec.shutdownNow();
    }

    @Override
    public void run() {
        mailbox.clear();
        started.compareAndSet(false, true);
        while (started.get()) {
            waitIfMailboxIsEmpty();
            Task task = mailbox.poll();
            if (task != null) {
                SimpleLogger.trace("Execute task : {}", task);
                task.apply();
            }
        }
        started.compareAndSet(true, false);
    }
    
    public void addTask(Task task) {
        if (task != null) {
            SimpleLogger.trace("New task waiting ... {}", task);
            mailbox.add(task);
            countRef.get().countDown();
        }
    }
    
    private void stopExecutor() {
        countRef.get().countDown();
        started.set(false);
    }

    private void waitIfMailboxIsEmpty() {
        if (mailbox.isEmpty()) {
            setLatchAndWait();
        }
    }

    private void setLatchAndWait() {
        if (countRef.get().getCount() == 0) {
            latch = new CountDownLatch(1);
            countRef.set(latch);
        }
        try {
            countRef.get().await();
        } catch (InterruptedException ex) {
            //ex.printStackTrace();
        }
    }
    
    public boolean isMailboxEmpty() {
        return mailbox.isEmpty();
    }
}
