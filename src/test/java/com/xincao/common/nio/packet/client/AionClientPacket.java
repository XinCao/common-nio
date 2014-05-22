package com.xincao.common.nio.packet.client;

import com.xincao.common.nio.core.AionConnection;
import com.xincao.common.nio.packet.BaseClientPacket;
import com.xincao.common.nio.packet.server.AionServerPacket;
import java.nio.ByteBuffer;

public abstract class AionClientPacket extends BaseClientPacket<AionConnection> {

    protected AionClientPacket(ByteBuffer buf, AionConnection client, int opcode) {
        super(buf, opcode);
        this.setConnection(client);
    }

    @Override
    public final void run() {
        try {
            this.runImpl();
        } catch (Throwable e) {
            logger.error("error handling client opcode = {} message" + getConnection().getIP());
        }
    }

    protected void sendPacket(AionServerPacket msg) {
        this.getConnection().sendPacket(msg);
    }
}