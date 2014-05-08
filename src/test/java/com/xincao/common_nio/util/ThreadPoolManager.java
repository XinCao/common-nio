package com.xincao.common_nio.util;

import com.xincao.common_nio.DisconnectionTask;
import com.xincao.common_nio.DisconnectionThreadPool;
import com.xincao.common_util.ScheduledThreadPoolExecutorAE;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadPoolManager implements DisconnectionThreadPool {

    private static final Logger log = LoggerFactory.getLogger(ThreadPoolManager.class);
    private static ThreadPoolManager instance = new ThreadPoolManager();
    private ScheduledThreadPoolExecutorAE scheduledThreadPool;
    private ScheduledThreadPoolExecutorAE disconnectionScheduledThreadPool;
    private ThreadPoolExecutor gameServerPacketsThreadPool;

    public static ThreadPoolManager getInstance() {
        return instance;
    }

    private ThreadPoolManager() {
        scheduledThreadPool = new ScheduledThreadPoolExecutorAE(4, new PriorityThreadFactory("ScheduledThreadPool", Thread.NORM_PRIORITY));
        disconnectionScheduledThreadPool = new ScheduledThreadPoolExecutorAE(4, new PriorityThreadFactory("DisconnectionScheduledThreadPool", Thread.NORM_PRIORITY));
        gameServerPacketsThreadPool = new ThreadPoolExecutor(4, Integer.MAX_VALUE, 5L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new PriorityThreadFactory("Game Server Packet Pool", Thread.NORM_PRIORITY + 3));
    }

    /**
     * 定时线程执行器（公共）
     *
     * @param <T>
     * @param r
     * @param delay
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T extends Runnable> ScheduledFuture<T> schedule(T r, long delay) {
        try {
            if (delay < 0) {
                delay = 0;
            }
            return (ScheduledFuture<T>) scheduledThreadPool.schedule(r, delay, TimeUnit.MILLISECONDS);
        } catch (RejectedExecutionException e) {
            return null;
        }
    }

    /**
     * 定时，且以固定频率执行的线程执行器（公共）
     *
     * @param <T>
     * @param r
     * @param initial
     * @param delay
     * @return ScheduledFuture
     */
    @SuppressWarnings("unchecked")
    public <T extends Runnable> ScheduledFuture<T> scheduleAtFixedRate(T r, long initial, long delay) {
        try {
            if (delay < 0) {
                delay = 0;
            }
            if (initial < 0) {
                initial = 0;
            }
            return (ScheduledFuture<T>) scheduledThreadPool.scheduleAtFixedRate(r, initial, delay, TimeUnit.MILLISECONDS);
        } catch (RejectedExecutionException e) {
            return null;
        }
    }

    /**
     * 客户端接受包，线程执行器（专有）
     *
     * @param pkt
     */
    public void executeGsPacket(Runnable pkt) {
        this.gameServerPacketsThreadPool.execute(pkt);
    }

    /**
     * 执行断开连接任务（专有）
     *
     * @param dt
     * @param delay
     */
    @Override
    public final void scheduleDisconnection(DisconnectionTask dt, long delay) {
        if (delay < 0) {
            delay = 0;
        }
        disconnectionScheduledThreadPool.schedule(dt, delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public void waitForDisconnectionTasks() {
        try {
            disconnectionScheduledThreadPool.shutdown();
            disconnectionScheduledThreadPool.awaitTermination(6, TimeUnit.MINUTES);
        } catch (Exception e) {
        }
    }

    /**
     * PriorityThreadFactory creating new threads for ThreadPoolManager
     *
     */
    private class PriorityThreadFactory implements ThreadFactory {

        private String name;
        private int prio;
        private AtomicInteger threadNumber = new AtomicInteger(1);
        private ThreadGroup group;

        public PriorityThreadFactory(String name, int prio) {
            this.prio = prio;
            this.name = name;
            group = new ThreadGroup(this.name);
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r);
            t.setName(name + "-" + threadNumber.getAndIncrement());
            t.setPriority(prio);
            t.setUncaughtExceptionHandler(new ThreadUncaughtExceptionHandler());
            return t;
        }
    }

    /**
     * Shutdown all thread pools.
     */
    public void shutdown() {
        try {
            scheduledThreadPool.shutdown();
            gameServerPacketsThreadPool.shutdown();
            scheduledThreadPool.awaitTermination(2, TimeUnit.SECONDS);
            gameServerPacketsThreadPool.awaitTermination(2, TimeUnit.SECONDS);
            log.info("All ThreadPools are now stopped");
        } catch (InterruptedException e) {
            log.error("Can't shutdown ThreadPoolManager", e);
        }
    }
}