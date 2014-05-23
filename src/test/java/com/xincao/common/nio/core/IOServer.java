package com.xincao.common.nio.core;

import com.xincao.common.nio.NioServer;
import com.xincao.common.nio.ServerCfg;
import com.xincao.common.nio.service.ThreadPoolManager;

public class IOServer {

	private final static NioServer instance;

	static {
		ServerCfg aionCfg = new ServerCfg("127.0.0.1", 8888,
				"Aion Connections", new AionConnectionFactoryImpl());
		instance = new NioServer(5, ThreadPoolManager.getInstance(), aionCfg);
	}

	public static NioServer getInstance() {
		return instance;
	}
}