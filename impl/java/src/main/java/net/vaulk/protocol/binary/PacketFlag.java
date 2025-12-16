package net.vaulk.protocol.binary;

import java.util.EnumSet;
import java.util.Set;

public enum PacketFlag {
    NONE((byte) 0x00),
    REQUIRES_ACK((byte) 0x10),
    IS_ACK((byte) 0x11);

    private final byte mask;

    PacketFlag(int mask) {
        this.mask = (byte) mask;
    }

    public byte mask() { return mask; }

    public static boolean has(byte mask, PacketFlag flag) {
        if (flag == null) return false;
        return (mask & flag.mask) != 0;
    }

    public static byte add(byte mask, PacketFlag flag) {
        if (flag == null) return mask;
        return (byte) (mask | flag.mask);
    }

    public static byte combine(PacketFlag... flags) {
        byte m = 0;
        if (flags == null) return m;

        for (PacketFlag f : flags) m |= f.mask();
        return m;
    }

    public static byte remove(byte mask, PacketFlag flag) {
        if (flag == null) return mask;
        return (byte) (mask & ~flag.mask);
    }

    public static EnumSet<PacketFlag> flagsOf(byte mask) {
        final EnumSet<PacketFlag> set;

        set = EnumSet.noneOf(PacketFlag.class);
        for (PacketFlag f : values()) {
            if (f == NONE) continue;
            if ((mask & f.mask) != 0) set.add(f);
        }
        return set;
    }

    public static byte toByte(Set<PacketFlag> set) {
        byte m = 0;

        if (set == null || set.isEmpty()) return NONE.mask;
        for (PacketFlag f : set) m |= f.mask;
        return m;
    }
}
