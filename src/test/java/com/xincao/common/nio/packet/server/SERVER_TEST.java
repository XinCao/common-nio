package com.xincao.common.nio.packet.server;

import com.xincao.common.nio.core.AionConnection;
import java.nio.ByteBuffer;

/**
 *
 * @author caoxin
 */
public class SERVER_TEST extends AionServerPacket {

    public SERVER_TEST(Integer opcode) {
        super(opcode);
    }

    @Override
    protected void writeImpl(AionConnection con, ByteBuffer buf) {
        this.writeS(buf, "hello world!");
    }
}