package com.xincao.common.nio;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;

/**
 * 监听器（IO seletor） （接受，可读，可写）
 *
 * @author 510655387@qq.com
 */
public abstract class Dispatcher extends Thread {

    protected final Object gate = new Object(); // Object on witch register vs selector.select are synchronized
    protected final Selector selector;

    public Dispatcher(String name) throws IOException {
        super(name);
        this.selector = SelectorProvider.provider().openSelector();
    }

    abstract protected void dispatch() throws IOException;

    @Override
    public void run() { // 经典服务器死循环
        while (true) {
            try {
                dispatch();
                synchronized (gate) {
                }
            } catch (IOException e) {
                Logger.error("Dispatcher error! " + e.getMessage());
            }
        }
    }

    public final Selector selector() {
        return this.selector;
    }
}
