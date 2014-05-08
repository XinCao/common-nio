package com.xincao.common_nio.util;

import com.xincao.common_util.ExitCode;
import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 死锁检查
 *
 * @author caoxin
 */
public class DeadLockDetector extends Thread {

    public enum Dealt {
        NOTHING(0),
        RESTART(1)
        ;
        private int id;

        private Dealt(int id) {
            this.id = id;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(DeadLockDetector.class);
    private final int sleepTime;
    private final ThreadMXBean tmx;
    private final Dealt doWhenDL;

    public DeadLockDetector(int sleepTime, Dealt dealt) {
        super("DeadLockDetector");
        this.sleepTime = sleepTime * 1000;
        this.tmx = ManagementFactory.getThreadMXBean();
        this.doWhenDL = dealt;
    }

    @Override
    public final void run() {
        boolean deadlock = false;
        while (!deadlock) {
            try {
                long[] ids = tmx.findDeadlockedThreads(); // 查找死锁
                if (ids != null) {
                    deadlock = true;
                    ThreadInfo[] tis = tmx.getThreadInfo(ids, true, true);
                    String info = "DeadLock Found!\n";
                    for (ThreadInfo ti : tis) {
                        info += ti.toString();
                    }
                    for (ThreadInfo ti : tis) {
                        LockInfo[] locks = ti.getLockedSynchronizers();
                        MonitorInfo[] monitors = ti.getLockedMonitors();
                        if (locks.length == 0 && monitors.length == 0) {
                            continue;
                        }
                        ThreadInfo dl = ti;
                        info += "Java-level deadlock:\n";
                        info += "\t" + dl.getThreadName() + " is waiting to lock " + dl.getLockInfo().toString() + " which is held by " + dl.getLockOwnerName() + "\n";
                        while ((dl = tmx.getThreadInfo(new long[]{dl.getLockOwnerId()}, true, true)[0]).getThreadId() != ti.getThreadId()) {
                            info += "\t" + dl.getThreadName() + " is waiting to lock " + dl.getLockInfo().toString() + " which is held by " + dl.getLockOwnerName() + "\n";
                        }
                    }
                    logger.warn(info);
                    if (doWhenDL == Dealt.RESTART) {
                        System.exit(ExitCode.CODE_RESTART);
                    }
                }
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                logger.warn("DeadLockDetector: " + e.getMessage(), e);
            }
        }
    }

    /**
     * 检查死锁
     *
     * @param second
     * @param dealt
     */
    public static void detector(int second, Dealt dealt) {
        new DeadLockDetector(second, dealt).start();
    }

    public static void main(String[] args) {
        detector(10, Dealt.RESTART);
        new Thread(new AThread(), "A thread").start();
        new Thread(new BThread(), "B thread").start();
    }

    private static int aThreadNum = 0;
    private static int bThreadNum = 0;
    
    static class AThread implements Runnable {

        @Override
        public void run() {
            aThreadNum ++;
            logger.info("a thread is the " + aThreadNum + "num");
            NeedObject.a();
            logger.info("a thread is the finish");
        }
    }

    static  class BThread implements Runnable {

        @Override
        public void run() {
            bThreadNum ++;
            logger.info("b thread is the " + bThreadNum + "num");
            NeedObject.b();
            logger.info("b thread is the finish");
        }
    }

    static class NeedObject {
        
        private static final Lock lockX = new ReentrantLock();
        private static final Lock lockY = new ReentrantLock();

        public static void a () {
            x();
            y();
            lockX.unlock();
            logger.info("ok a");
        }

        public static void b () {
            y();
            x();
            lockY.unlock();
            logger.info("ok b");
        }

        public static void x () {
            lockX.lock();
            try {
                Thread.sleep(1000 * 2);
            } catch (InterruptedException ex) {
                logger.error(ex.getMessage());
            }
        }
        
        public static void y () {
            lockY.lock();
            try {
                Thread.sleep(1000 * 5);
            } catch (InterruptedException ex) {
                logger.error(ex.getMessage());
            }
        }
    }
}