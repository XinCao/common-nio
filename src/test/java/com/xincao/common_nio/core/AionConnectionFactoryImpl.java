package com.xincao.common_nio.core;

import com.xincao.common_nio.AConnection;
import com.xincao.common_nio.ConnectionFactory;
import com.xincao.common_nio.Dispatcher;
import java.io.IOException;
import java.nio.channels.SocketChannel;

public class AionConnectionFactoryImpl implements ConnectionFactory {

    @Override
    public AConnection create(SocketChannel socket, Dispatcher dispatcher) throws IOException {
        return new AionConnection(socket, dispatcher);
    }
}