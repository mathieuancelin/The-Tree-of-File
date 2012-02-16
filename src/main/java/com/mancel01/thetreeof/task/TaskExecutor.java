package com.mancel01.thetreeof.task;

import com.mancel01.thetreeof.api.Task;
import com.mancel01.thetreeof.util.SimpleLogger;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class TaskExecutor extends Thread {
    
    private CountDownLatch latch;
    
    private final ConcurrentLinkedQueue<Task> mailbox =
                new ConcurrentLinkedQueue<Task>();
    
    private final AtomicBoolean started = new AtomicBoolean(false);
    
    private final ExecutorService exec = Executors.newCachedThreadPool();

    public TaskExecutor() {
        latch = new CountDownLatch(1);
    }
    
    public static void startTaskExecutor(TaskExecutor executor) {
        executor.exec.submit(executor);
    }
    
    public static void stopTaskExecutor(TaskExecutor executor) {
        executor.stopExecutor();
        executor.exec.shutdown();
    }

    @Override
    public void run() {
        mailbox.clear();
        started.compareAndSet(false, true);
        while (started.get()) {
            waitIfMailboxIsEmpty();
            Task task = mailbox.poll();
            if (task != null) {
                latch = new CountDownLatch(1);
                SimpleLogger.trace("Execute task : {}", task);
                task.apply();
                latch.countDown();
            }
        }
        started.compareAndSet(true, false);
    }
    
    public void addTask(Task task) {
        if (task != null) {
            SimpleLogger.trace("New task waiting ... {}", task);
            mailbox.add(task);
        }
    }
    
    private void stopExecutor() {
        started.set(false);
    }

    private void waitIfMailboxIsEmpty() {
        while(mailbox.isEmpty()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public boolean isMailboxEmpty() {
        return mailbox.isEmpty();
    }
    
    public void waitForLastTask() {
        try {
            latch.await();
        } catch (InterruptedException ex) { }
    }
}
