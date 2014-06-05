package com.xincao.common.nio;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 监听器（IO seletor） （接受，可读，可写）
 *
 * @author caoxin
 */
public abstract class Dispatcher extends Thread {

    protected final Object gate = new Object(); // Object on witch register vs selector.select are synchronized
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
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
                logger.error("Dispatcher error! " + e, e);
            }
        }
    }

    public final Selector selector() {
        return this.selector;
    }
}
