package com.xincao.common.nio;

/**
 * 断开连接任务(例如，备份)
 *
 * @author caoxin
 */
public class DisconnectionTask implements Runnable {

    private final IConnection connection;

    public DisconnectionTask(IConnection connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        connection.onDisconnect();
    }
}