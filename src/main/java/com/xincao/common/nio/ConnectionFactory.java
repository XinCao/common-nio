package com.xincao.common.nio;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * 创建连接对象
 * 
 * @author caoxin
 */
public interface ConnectionFactory {

	public IConnection create(SocketChannel socket, IODispatcher ioDispatcher)
			throws IOException;
}