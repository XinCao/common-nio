/**
 * 欢迎浏览和修改代码，有任何想法可以email我
 */
package com.br.common.nio;

/**
 * 断开连接任务(例如，备份)
 *
 * @author 510655387@qq.com
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