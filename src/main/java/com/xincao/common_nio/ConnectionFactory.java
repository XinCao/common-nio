package com.xincao.common_nio;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * 创建连接对象
 *
 * @author caoxin
 */
public interface ConnectionFactory {

    public AConnection create(SocketChannel socket, Dispatcher dispatcher) throws IOException;
}