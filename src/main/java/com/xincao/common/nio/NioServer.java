package com.xincao.common.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NioServer {

	private static final Logger logger = LoggerFactory
			.getLogger(NioServer.class.getName());
	private AcceptDispatcher acceptDispatcher;
	private final ServerCfg[] cfgs;
	private int currentIODispatcher = 0;
	private final DisconnectionThreadPool dcPool;
	private IODispatcher[] ioDispatchers;
	private int ioThreads = 5;
	private final List<SelectionKey> serverChannelKeys = new ArrayList<>();

	public NioServer(int ioThreads, DisconnectionThreadPool dcPool,
			ServerCfg... cfgs) {
		this.dcPool = dcPool;
		if (ioThreads > 0) {
			this.ioThreads = ioThreads;
		} else {
			logger.info("ioThreads num is default = {}", this.ioThreads);
		}
		this.cfgs = cfgs;
	}

	/**
	 * Close all active connections. （服务器关闭，对所有的连接进行一些备份工作）
	 */
	private void closeAll() {
		if (ioDispatchers != null) {
			for (Dispatcher d : ioDispatchers) {
				for (SelectionKey key : d.selector().keys()) {
					if (key.attachment() instanceof IConnection) {
						((IConnection) key.attachment()).close(true);
					}
				}
			}
		}
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
					logger.info("Server listening on all available IPs on Port "
							+ cfg.port + " for " + cfg.connectionName);
				} else {
					isa = new InetSocketAddress(cfg.hostName, cfg.port);
					logger.info("Server listening on IP: " + cfg.hostName
							+ " Port " + cfg.port + " for "
							+ cfg.connectionName);
				}
				serverChannel.socket().bind(isa);
				SelectionKey acceptKey = getAcceptDispatcher().register(
						serverChannel, SelectionKey.OP_ACCEPT,
						new Acceptor(cfg.factory, this));
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
	public final AcceptDispatcher getAcceptDispatcher() {
		return acceptDispatcher;
	}

	/**
	 * 返回活跃连接个数
	 * 
	 * @return
	 */
	public final int getActiveConnections() {
		int count = 0;
		if (ioDispatchers != null) {
			for (Dispatcher d : ioDispatchers) {
				count += d.selector().keys().size();
			}
		} else {
			logger.error("ioDispatchers array is null");
		}
		return count;
	}

	/**
	 * 获得一个连接读写监听器（每个监听器的负载均衡）
	 * 
	 * @return
	 */
	public final IODispatcher getIODispatcher() {
		if (ioDispatchers.length == 1) {
			return ioDispatchers[0];
		}
		if (currentIODispatcher >= ioThreads) {
			currentIODispatcher = 0;
		}
		return ioDispatchers[currentIODispatcher++];
	}

	/**
	 * Calls onServerClose method for all active connections.（服务器关闭，对所有的连接进行通知）
	 */
	private void notifyServerClose() {
		if (ioDispatchers != null) {
			for (Dispatcher d : ioDispatchers) {
				for (SelectionKey key : d.selector().keys()) {
					if (key.attachment() instanceof IConnection) {
						((IConnection) key.attachment()).onServerClose();
					}
				}
			}
		}
	}

	/**
	 * 服务器关闭
	 */
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
		} catch (InterruptedException t) {
			logger.warn("Nio thread was interrupted during shutdown", t);
		}
		logger.info(" Active connections: " + getActiveConnections());
		logger.info("Forced Disconnecting all connections...");
		this.closeAll();
		logger.info(" Active connections: " + getActiveConnections());
		dcPool.waitForDisconnectionTasks();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException t) {
			logger.warn("Nio thread was interrupted during shutdown", t);
		}
	}

	/**
	 * 初始化线程
	 * 
	 * @param readWriteThreads
	 * @param dcPool
	 * @throws IOException
	 */
	private void startDispatchers(DisconnectionThreadPool dcPool)
			throws IOException {
		acceptDispatcher = new AcceptDispatcher("Accept Dispatcher");
		acceptDispatcher.start();
		ioDispatchers = new IODispatcher[this.ioThreads];
		for (int i = 0; i < this.ioThreads; i++) {
			ioDispatchers[i] = new IODispatcher("IO-" + i + " Dispatcher",
					dcPool);
			ioDispatchers[i].start();
		}
	}
}