package com.xincao.common_nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NioServer {

    private static final Logger logger = LoggerFactory.getLogger(NioServer.class.getName());
    private final List<SelectionKey> serverChannelKeys = new ArrayList<SelectionKey>();
    private AcceptDispatcherImpl acceptDispatcher;
    private int currentReadWriteDispatcher = 0;
    private Dispatcher[] readWriteDispatchers;
    private final DisconnectionThreadPool dcPool;
    private int readWriteThreads = 5;
    private ServerCfg[] cfgs;

    public NioServer(int readWriteThreads, DisconnectionThreadPool dcPool, ServerCfg... cfgs) {
        this.dcPool = dcPool;
        if (readWriteThreads > 0) {
            this.readWriteThreads = readWriteThreads;
        } else {
            logger.info("readWriteThreads num is default = {}", this.readWriteThreads);
        }
        this.cfgs = cfgs;
    }

    /**
     * 启动NIO-Server
     */
    public void connect() {
        try {
            this.startDispatchers(dcPool);
            for (ServerCfg cfg : cfgs) {
                ServerSocketChannel serverChannel = ServerSocketChannel.open();
                serverChannel.configureBlocking(false);
                InetSocketAddress isa;
                if ("*".equals(cfg.hostName)) {
                    isa = new InetSocketAddress(cfg.port);
                    logger.info("Server listening on all available IPs on Port " + cfg.port + " for " + cfg.connectionName);
                } else {
                    isa = new InetSocketAddress(cfg.hostName, cfg.port);
                    logger.info("Server listening on IP: " + cfg.hostName + " Port " + cfg.port + " for " + cfg.connectionName);
                }
                serverChannel.socket().bind(isa);
                SelectionKey acceptKey = getAcceptDispatcher().register(serverChannel, SelectionKey.OP_ACCEPT, new Acceptor(cfg.factory, this));
                serverChannelKeys.add(acceptKey);
            }
        } catch (IOException e) {
            logger.error("NioServer Initialization Error: " + e, e);
            throw new Error("NioServer Initialization Error!");
        }
    }

    /**
     * 获得连接接受监听器
     * 
     * @return 
     */
    public final AcceptDispatcherImpl getAcceptDispatcher() {
        return acceptDispatcher;
    }

    /**
     * 获得一个连接读写监听器（每个监听器的负载均衡）
     * 
     * @return 
     */
    public final Dispatcher getReadWriteDispatcher() {
        if (readWriteDispatchers.length == 1) {
            return readWriteDispatchers[0];
        }
        if (currentReadWriteDispatcher >= readWriteThreads) {
            currentReadWriteDispatcher = 0;
        }
        return readWriteDispatchers[currentReadWriteDispatcher++];
    }

    /**
     * 初始化线程
     * 
     * @param readWriteThreads
     * @param dcPool
     * @throws IOException 
     */
    private void startDispatchers(DisconnectionThreadPool dcPool) throws IOException {
        acceptDispatcher = new AcceptDispatcherImpl("Accept Dispatcher");
        acceptDispatcher.start();
        readWriteDispatchers = new Dispatcher[this.readWriteThreads];
        for (int i = 0; i < this.readWriteThreads; i++) {
        readWriteDispatchers[i] = new AcceptReadWriteDispatcherImpl("ReadWrite-" + i + " Dispatcher", dcPool);
        readWriteDispatchers[i].start();
        }
    }

    /**
     * 返回活跃连接个数
     *
     * @return 
     */
    public final int getActiveConnections() {
        int count = 0;
        if (readWriteDispatchers != null) {
            for (Dispatcher d : readWriteDispatchers) {
                count += d.selector().keys().size();
            }
        } else {
            logger.error("readWriteDispatchers array is null");
        }
        return count;
    }

    public final void shutdown() {
        logger.info("Closing ServerChannels...");
        try {
            for (SelectionKey key : serverChannelKeys) {
                key.cancel();
            }
            logger.info("ServerChannel closed.");
        } catch (Exception e) {
            logger.error("Error during closing ServerChannel, " + e, e);
        }
        this.notifyServerClose();
        try {
            Thread.sleep(1000);
        } catch (Throwable t) {
            logger.warn("Nio thread was interrupted during shutdown", t);
        }
        logger.info(" Active connections: " + getActiveConnections());
        logger.info("Forced Disconnecting all connections...");
        this.closeAll();
        logger.info(" Active connections: " + getActiveConnections());
        dcPool.waitForDisconnectionTasks();
        try {
            Thread.sleep(1000);
        } catch (Throwable t) {
            logger.warn("Nio thread was interrupted during shutdown", t);
        }
    }

    /**
     * Calls onServerClose method for all active connections.
     */
    private void notifyServerClose() {
        if (readWriteDispatchers != null) {
            for (Dispatcher d : readWriteDispatchers) {
                for (SelectionKey key : d.selector().keys()) {
                    if (key.attachment() instanceof AConnection) {
                        ((AConnection) key.attachment()).onServerClose();
                    }
                }
            }
        }
    }

    /**
     * Close all active connections.
     */
    private void closeAll() {
        if (readWriteDispatchers != null) {
            for (Dispatcher d : readWriteDispatchers) {
                for (SelectionKey key : d.selector().keys()) {
                    if (key.attachment() instanceof AConnection) {
                        ((AConnection) key.attachment()).close(true);
                    }
                }
            }
        }
    }
}