/**
 * 欢迎浏览和修改代码，有任何想法可以email我
 */
package com.br.common.nio.packet;

import java.nio.ByteBuffer;

/**
 * 
 * @author 510655387@qq.com
 */
public abstract class BaseServerPacket extends BasePacket {

    protected BaseServerPacket() {
        super(PacketType.SERVER);
    }

    protected BaseServerPacket(int opcode) {
        super(PacketType.SERVER, opcode);
    }

    protected final void writeB(ByteBuffer buf, byte[] data) {
        buf.put(data);
    }

    protected final void writeC(ByteBuffer buf, int value) {
        buf.put((byte) value);
    }

    protected final void writeD(ByteBuffer buf, int value) {
        buf.putInt(value);
    }

    protected final void writeDF(ByteBuffer buf, double value) {
        buf.putDouble(value);
    }

    protected final void writeF(ByteBuffer buf, float value) {
        buf.putFloat(value);
    }

    protected final void writeH(ByteBuffer buf, int value) {
        buf.putShort((short) value);
    }

    protected final void writeQ(ByteBuffer buf, long value) {
        buf.putLong(value);
    }

    protected final void writeS(ByteBuffer buf, String text) {
        if (text == null) {
            buf.putChar('\000');
        } else {
            final int len = text.length();
            for (int i = 0; i < len; i++) {
                buf.putChar(text.charAt(i));
            }
            buf.putChar('\000');
        }
    }
}