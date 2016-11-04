/**
 * 欢迎浏览和修改代码，有任何想法可以email我
 */
package com.br.common.nio;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * 连接接收器
 *
 * @author 510655387@qq.com
 */
public class Acceptor {

    private final ConnectionFactory factory;
    private final NioServer nioServer;

    Acceptor(ConnectionFactory factory, NioServer nioServer) {
        this.factory = factory;
        this.nioServer = nioServer;
    }

    public final void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        IODispatcher ioDispatcher = nioServer.getIODispatcher(); // 获得一个读写监听器
        factory.create(socketChannel, ioDispatcher);
    }
}