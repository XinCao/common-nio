package com.xincao.common.nio.core;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import com.xincao.common.nio.ConnectionFactory;
import com.xincao.common.nio.IConnection;
import com.xincao.common.nio.IODispatcher;

public class AionConnectionFactoryImpl implements ConnectionFactory {

	@Override
	public IConnection create(SocketChannel socket, IODispatcher ioDispatcher)
			throws IOException {
		return new AionConnection(socket, ioDispatcher);
	}
}