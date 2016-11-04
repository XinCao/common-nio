package com.xincao.common.nio.packet;

import java.nio.ByteBuffer;

import com.xincao.common.nio.IConnection;
import com.xincao.common.nio.Logger;

public abstract class BaseClientPacket<T extends IConnection> extends BasePacket implements Runnable {

    private ByteBuffer buf;
    private T client;

    public BaseClientPacket(ByteBuffer buf, int opcode) {
        this(opcode);
        this.buf = buf;
    }

    public BaseClientPacket(int opcode) {
        super(PacketType.CLIENT, opcode);
    }

    public final T getConnection() {
        return client;
    }

    public final int getRemainingBytes() {
        return buf.remaining();
    }

    public final boolean read() {
        try {
            readImpl();
            if (getRemainingBytes() > 0) {
                Logger.info("Packet " + this + " not fully readed!");
            }

            return true;
        } catch (Exception re) {
            Logger.error("Reading failed for packet " + this + re.getMessage());
            return false;
        }
    }

    protected final byte[] readB(int length) {
        byte[] result = new byte[length];
        try {
            buf.get(result);
        } catch (Exception e) {
            Logger.error("Missing byte[] for: " + this);
        }
        return result;
    }

    protected final int readC() {
        try {
            return buf.get() & 0xFF;
        } catch (Exception e) {
            Logger.error("Missing C for: " + this);
        }
        return 0;
    }

    protected final int readD() {
        try {
            return buf.getInt();
        } catch (Exception e) {
            Logger.error("Missing D for: " + this);
        }
        return 0;
    }

    protected final double readDF() {
        try {
            return buf.getDouble();
        } catch (Exception e) {
            Logger.error("Missing DF for: " + this);
        }
        return 0;
    }

    protected final float readF() {
        try {
            return buf.getFloat();
        } catch (Exception e) {
            Logger.error("Missing F for: " + this);
        }
        return 0;
    }

    protected final int readH() {
        try {
            return buf.getShort() & 0xFFFF;
        } catch (Exception e) {
            Logger.error("Missing H for: " + this);
        }
        return 0;
    }

    protected abstract void readImpl();

    protected final long readQ() {
        try {
            return buf.getLong();
        } catch (Exception e) {
            Logger.error("Missing Q for: " + this);
        }
        return 0;
    }

    protected final String readS() {
        StringBuilder sb = new StringBuilder();
        char ch;
        try {
            while ((ch = buf.getChar()) != 0) {
                sb.append(ch);
            }
        } catch (Exception e) {
            Logger.error("Missing S for: " + this);
        }
        return sb.toString();
    }

    protected abstract void runImpl();

    public void setBuffer(ByteBuffer buf) {
        this.buf = buf;
    }

    public void setConnection(T client) {
        this.client = client;
    }
}
