package com.xincao.common.nio.packet.server;

import java.nio.ByteBuffer;

import com.xincao.common.nio.core.AionConnection;
import com.xincao.common.nio.packet.BaseServerPacket;

public abstract class AionServerPacket extends BaseServerPacket {

    protected AionServerPacket(int opcode) {
        super(opcode);
    }

    public final void write(AionConnection con, ByteBuffer buf) {
        int startPosition = buf.position();
        buf.putShort((short) 0);
        buf.put((byte) getOpcode());
        this.writeImpl(con, buf);
        int endPosition = buf.position();
        short size = (short) (endPosition - startPosition);
        buf.position(startPosition);
        buf.putShort(size);
        buf.position(endPosition);
        buf.flip();
    }

    protected abstract void writeImpl(AionConnection con, ByteBuffer buf);
}