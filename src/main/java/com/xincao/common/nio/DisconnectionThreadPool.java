package com.xincao.common.nio;

public interface DisconnectionThreadPool {

	public void scheduleDisconnection(DisconnectionTask dt, long delay);

	public void waitForDisconnectionTasks();
}