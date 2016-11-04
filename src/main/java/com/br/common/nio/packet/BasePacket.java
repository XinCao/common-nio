package com.br.common.nio.packet;

public abstract class BasePacket {

    public static enum PacketType {

        CLIENT("C"), SERVER("S");
        private final String name;

        private PacketType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static final String TYPE_PATTERN = "[%s] 0x%02X %s";
    private int opcode;

    private final PacketType packetType;

    protected BasePacket(PacketType packetType) {
        this.packetType = packetType;
    }

    protected BasePacket(PacketType packetType, int opcode) {
        this.packetType = packetType;
        this.opcode = opcode;
    }

    public final int getOpcode() {
        return opcode;
    }

    public String getPacketName() {
        return this.getClass().getSimpleName();
    }

    public final PacketType getPacketType() {
        return packetType;
    }

    protected void setOpcode(int opcode) {
        this.opcode = opcode;
    }

    @Override
    public String toString() {
        return String.format(TYPE_PATTERN, getPacketType().getName(), getOpcode(), getPacketName());
    }
}
