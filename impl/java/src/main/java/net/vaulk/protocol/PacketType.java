package net.vaulk.protocol;

import java.util.HashMap;
import java.util.Map;

public enum PacketType {
    PLACEHOLDER(0x70);

    private static final Map<Byte, PacketType> BY_ID = new HashMap<>();

    static {
        for(PacketType t : values())
            BY_ID.put(t.packetId, t);
    }

    private final byte packetId;

    PacketType(int packetId) {
        this.packetId = (byte) packetId;
    }

    public byte getPacketId() {
        return packetId;
    }

    public static PacketType fromId(byte packetId) {
        return BY_ID.getOrDefault(packetId, null);
    }
}
