/**
 * 欢迎浏览和修改代码，有任何想法可以email我
 */
package com.br.common.nio;

/**
 * 
 * @author 510655387@qq.com
 */
public interface DisconnectionThreadPool {

    public void scheduleDisconnection(DisconnectionTask dt, long delay);

    public void waitForDisconnectionTasks();
}