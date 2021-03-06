/**
 * 欢迎浏览和修改代码，有任何想法可以email我
 */
package com.br.common.nio;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.br.common.nio.packet.BaseClientPacket;

/**
 * 
 * @author 510655387@qq.com
 * @param <T>
 */
public class PacketProcessor<T extends IConnection> {

    private final class CheckerTask implements Runnable {

        private int lastSize = 0;
        private final int sleepTime = 60 * 1000; // 一分钟检测一次，这个值，直接关系到，线程池中工作线程的个数，和对请求处理的敏捷程度(如果，对于高并发的需求则需要降低，以提高敏捷度)

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                }
                int sizeNow = packets.size();
                if (sizeNow < lastSize) {
                    if (sizeNow < reduceThreshold) { // 降低阈值
                        killThread();
                    }
                } else if (sizeNow > lastSize && sizeNow > increaseThreshold) {
                    if (!newThread() && sizeNow >= increaseThreshold * 3) {
                        Logger.info("Lagg detected! ["
                                + sizeNow
                                + " client packets are waiting for execution]. You should consider increasing PacketProcessor maxThreads or hardware upgrade.");
                    }
                }
                lastSize = sizeNow;
            }
        }
    }

    private final class PacketProcessorTask implements Runnable {

        @Override
        public void run() {
            BaseClientPacket<T> packet = null;
            while (true) {
                lock.lock();
                try {
                    if (packet != null) {
                        packet.getConnection().unlockConnection();
                    }
                    if (Thread.interrupted()) {
                        return;
                    }
                    packet = getFirstAviable();
                } finally {
                    lock.unlock();
                }
                packet.run();
            }
        }
    }

    private final static int increaseThreshold = 50; // 最大阈值
    private final static int reduceThreshold = 3; // 最小阈值
    private final Lock lock = new ReentrantLock();
    private final int maxThreads;
    private final int minThreads;
    private final Condition notEmpty = lock.newCondition();

    private final List<BaseClientPacket<T>> packets = new LinkedList<BaseClientPacket<T>>();

    private final List<Thread> threads = new ArrayList<Thread>();

    public PacketProcessor(int minThreads, int maxThreads) {
        if (minThreads <= 0) {
            minThreads = 1;
        }
        if (maxThreads < minThreads) {
            maxThreads = minThreads;
        }
        this.minThreads = minThreads;
        this.maxThreads = maxThreads;
        if (minThreads != maxThreads) {
            startCheckerThread();
        }
        for (int i = 0; i < minThreads; i++) {
            newThread();
        }
    }

    public final void executePacket(BaseClientPacket<T> packet) {
        lock.lock();
        try {
            packets.add(packet);
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    private BaseClientPacket<T> getFirstAviable() {
        while (true) {
            while (packets.isEmpty()) {
                notEmpty.awaitUninterruptibly();
            }
            ListIterator<BaseClientPacket<T>> it = packets.listIterator();
            while (it.hasNext()) {
                BaseClientPacket<T> packet = it.next();
                if (packet.getConnection().tryLockConnection()) {
                    it.remove();
                    return packet;
                }
            }
            notEmpty.awaitUninterruptibly();
        }
    }

    private void killThread() {
        if (threads.size() < minThreads) {
            Thread t = threads.remove((threads.size() - 1));
            Logger.info("Killing PacketProcessor Thread: " + t.getName());
            t.interrupt();
        }
    }

    private boolean newThread() {
        if (threads.size() >= maxThreads) {
            return false;
        }
        String name = "PacketProcessor:" + threads.size();
        Logger.info("Creating new PacketProcessor Thread: " + name);
        Thread t = new Thread(new PacketProcessorTask(), name);
        threads.add(t);
        t.start();
        return true;
    }

    private void startCheckerThread() {
        new Thread(new CheckerTask(), "PacketProcessor:Checker").start();
    }
}
