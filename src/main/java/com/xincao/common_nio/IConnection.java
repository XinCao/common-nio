package com.xincao.common_nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public abstract class IConnection {

    private final SocketChannel socketChannel;
    private final IODispatcher ioDispatcher;
    private SelectionKey key;
    protected boolean pendingClose;
    protected boolean isForcedClosing;
    protected boolean closed; // 关闭套接字管道
    protected final Object guard = new Object();
    public final ByteBuffer writeBuffer;
    public final ByteBuffer readBuffer;
    private final String ip;
    private boolean locked = false;

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

    protected final void enableWriteInterest() {
        if (key.isValid()) {
            key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
            key.selector().wakeup();
        }
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

    protected final boolean isPendingClose() {
        return pendingClose && !closed;
    }

    protected final boolean isWriteDisabled() {
        return pendingClose || closed;
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

    private IODispatcher getIODispatcher() {
        return ioDispatcher;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public final void setKey(SelectionKey key) {
        this.key = key;
    }

    public final String getIP() {
        return ip;
    }

    abstract protected boolean processData(ByteBuffer data);

    abstract protected boolean writeData(ByteBuffer data);

    abstract protected long getDisconnectionDelay();

    abstract protected void onDisconnect();

    abstract protected void onServerClose();
}