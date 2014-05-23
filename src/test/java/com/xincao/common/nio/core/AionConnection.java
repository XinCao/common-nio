package com.xincao.common.nio.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Deque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xincao.common.nio.IConnection;
import com.xincao.common.nio.IODispatcher;
import com.xincao.common.nio.PacketProcessor;
import com.xincao.common.nio.core.AionPacketHandler.AionServerKind;
import com.xincao.common.nio.packet.client.AionClientPacket;
import com.xincao.common.nio.packet.server.AionServerPacket;

public class AionConnection extends IConnection {

	public static enum State {
		AUTHED_GG, AUTHED_LOGIN, CONNECTED
	}

	private static final Logger log = LoggerFactory
			.getLogger(AionConnection.class);
	private final static PacketProcessor<AionConnection> processor = new PacketProcessor<AionConnection>(
			1, 8);
	private boolean joinedGs;
	private final Deque<AionServerPacket> sendMsgQueue = new ArrayDeque<AionServerPacket>();
	private final int sessionId = hashCode();

	private State state;

	public AionConnection(SocketChannel sc, IODispatcher ioDispatcher)
			throws IOException {
		super(sc, ioDispatcher);
		state = State.CONNECTED;
		String ip = getIP();
		log.info("connection from: " + ip);
		AionServerPacket asp = AionPacketHandler.getServerPacketByASK(
				AionServerKind.SERVER_TEST, this);
		sendPacket(asp);
	}

	public final void close(AionServerPacket closePacket, boolean forced) {
		synchronized (guard) {
			if (isWriteDisabled()) {
				return;
			}
			log.info("sending packet: " + closePacket
					+ " and closing connection after that.");
			pendingClose = true;
			isForcedClosing = forced;
			sendMsgQueue.clear();
			sendMsgQueue.addLast(closePacket);
			enableWriteInterest();
		}
	}

	@Override
	protected final long getDisconnectionDelay() {
		return 0;
	}

	public final int getSessionId() {
		return sessionId;
	}

	public final State getState() {
		return state;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
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

	@Override
	protected final boolean processData(ByteBuffer data) {
		AionClientPacket pck = AionPacketHandler.handle(data, this);
		log.info("recived packet: " + pck);
		if ((pck != null) && pck.read()) {
			processor.executePacket(pck);
		}
		return true;
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

	public final void setJoinedGs() {
		joinedGs = true;
	}

	public final void setState(State state) {
		this.state = state;
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
}