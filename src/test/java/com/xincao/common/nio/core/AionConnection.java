package com.xincao.common.nio.core;

import com.xincao.common.nio.IODispatcher;
import com.xincao.common.nio.IConnection;
import com.xincao.common.nio.PacketProcessor;
import com.xincao.common.nio.core.AionPacketHandler.AionServerKind;
import com.xincao.common.nio.packet.client.AionClientPacket;
import com.xincao.common.nio.packet.server.AionServerPacket;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Deque;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AionConnection extends IConnection {

    private static final Logger log = LoggerFactory.getLogger(AionConnection.class);
    private final static PacketProcessor<AionConnection> processor = new PacketProcessor<AionConnection>(1, 8);
    private final Deque<AionServerPacket> sendMsgQueue = new ArrayDeque<AionServerPacket>();
    private final int sessionId = hashCode();
    private boolean joinedGs;
    private State state;

    public static enum State {
        CONNECTED,
        AUTHED_GG,
        AUTHED_LOGIN
    }

    public AionConnection(SocketChannel sc, IODispatcher ioDispatcher )throws IOException {
        super(sc, ioDispatcher);
        state = State.CONNECTED;
        String ip = getIP();
        log.info("connection from: " + ip);
        AionServerPacket asp = AionPacketHandler.getServerPacketByASK(AionServerKind.SERVER_TEST, this);
        sendPacket(asp);
    }

    @Override
    protected final boolean processData(ByteBuffer data) {
        AionClientPacket pck = AionPacketHandler.handle(data, this);
        log.info("recived packet: " + pck);
        if ((pck != null) && pck.read()) {
            processor.executePacket(pck);
        }
        return true;
    }

    @Override
    protected final boolean writeData(ByteBuffer data) {
        synchronized (guard) {
            AionServerPacket packet = sendMsgQueue.pollFirst();
            if (packet == null) {
                return false;
            }
            packet.write(this, data);
            return true;
        }
    }

    @Override
    protected final long getDisconnectionDelay() {
        return 0;
    }

    /**
     * DisconnectionTask 调用（可以添加服务逻辑）
     */
    @Override
    protected final void onDisconnect() {
    }

    /**
     * shutdown 调用 （可以添加服务逻辑）
     */
    @Override
    protected final void onServerClose() {
        close(true);
    }

    public final void sendPacket(AionServerPacket bp) {
        synchronized (guard) {
            if (isWriteDisabled()) {
                return;
            }
            log.debug("sending packet: " + bp);
            sendMsgQueue.addLast(bp);
            enableWriteInterest();
        }
    }

    public final void close(AionServerPacket closePacket, boolean forced) {
        synchronized (guard) {
            if (isWriteDisabled()) {
                return;
            }
            log.info("sending packet: " + closePacket + " and closing connection after that.");
            pendingClose = true;
            isForcedClosing = forced;
            sendMsgQueue.clear();
            sendMsgQueue.addLast(closePacket);
            enableWriteInterest();
        }
    }

    public final void setJoinedGs() {
        joinedGs = true;
    }

    public final void setState(State state) {
        this.state = state;
    }

    public final State getState() {
        return state;
    }

    public final int getSessionId() {
        return sessionId;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}