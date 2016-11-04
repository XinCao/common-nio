/**
 * 欢迎浏览和修改代码，有任何想法可以email我
 */
package com.br.common.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * 连接对象（整个网络层向项目中暴漏的唯一对象）
 *
 * @author 510655387@qq.com
 */
public abstract class IConnection {

    protected boolean closed; // 关闭套接字管道
    protected final Object guard = new Object();
    private final IODispatcher ioDispatcher;
    private final String ip;
    protected boolean isForcedClosing;
    private SelectionKey key;
    private boolean locked = false;
    protected boolean pendingClose;
    public final ByteBuffer readBuffer;
    private final SocketChannel socketChannel;
    public final ByteBuffer writeBuffer;
    private Object object;

    public IConnection(SocketChannel sc, IODispatcher ioDispatcher) throws IOException {
        socketChannel = sc;
        this.ioDispatcher = ioDispatcher;
        writeBuffer = ByteBuffer.allocate(8192 * 2);
        writeBuffer.flip();
        writeBuffer.order(ByteOrder.LITTLE_ENDIAN);
        readBuffer = ByteBuffer.allocate(8192 * 2);
        readBuffer.order(ByteOrder.LITTLE_ENDIAN);
        this.ioDispatcher.register(socketChannel, SelectionKey.OP_READ, this);
        this.ip = socketChannel.socket().getInetAddress().getHostAddress();
    }

    /**
     * 关闭连接
     *
     * @param forced
     */
    public final void close(boolean forced) {
        synchronized (guard) {
            if (isWriteDisabled()) {
                return;
            }
            isForcedClosing = forced;
            getIODispatcher().closeConnection(this);
        }
    }

    /**
     * 关闭套接字管道
     */
    public final boolean closeSocketChannel() {
        synchronized (guard) {
            if (closed) {
                return false;
            }
            try {
                if (socketChannel.isOpen()) {
                    socketChannel.close();
                    key.attach(null);
                    key.cancel();
                }
                closed = true;
            } catch (IOException ignored) {
            }
        }
        return true;
    }

    protected final void enableWriteInterest() {
        if (key.isValid()) {
            key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
            key.selector().wakeup();
        }
    }

    abstract protected long getDisconnectionDelay();

    private IODispatcher getIODispatcher() {
        return ioDispatcher;
    }

    public final String getIP() {
        return ip;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    protected final boolean isPendingClose() {
        return pendingClose && !closed;
    }

    protected final boolean isWriteDisabled() {
        return pendingClose || closed;
    }

    abstract protected void onDisconnect();

    abstract protected void onServerClose();

    abstract protected boolean processData(ByteBuffer data);

    public final void setKey(SelectionKey key) {
        this.key = key;
    }

    public boolean tryLockConnection() {
        if (locked) {
            return false;
        }
        return locked = true;
    }

    public void unlockConnection() {
        locked = false;
    }

    abstract protected boolean writeData(ByteBuffer data);

    public <T extends Object> void setObject(T object) {
        this.object = object;
    }

    public <T extends Object> T getObject() {
        return (T)this.object;
    }
}