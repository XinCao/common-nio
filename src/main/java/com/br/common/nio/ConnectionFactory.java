/**
 * 欢迎浏览和修改代码，有任何想法可以email我
 */
package com.br.common.nio;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * 创建连接对象
 *
 * @author 510655387@qq.com
 */
public interface ConnectionFactory {

    public IConnection create(SocketChannel socket, IODispatcher ioDispatcher) throws IOException;
}