package com.xincao.common_nio.core;

import com.xincao.common_nio.IConnection;
import com.xincao.common_nio.ConnectionFactory;
import com.xincao.common_nio.IODispatcher;
import java.io.IOException;
import java.nio.channels.SocketChannel;

public class AionConnectionFactoryImpl implements ConnectionFactory {

    @Override
    public IConnection create(SocketChannel socket, IODispatcher ioDispatcher) throws IOException {
        return new AionConnection(socket, ioDispatcher);
    }
}