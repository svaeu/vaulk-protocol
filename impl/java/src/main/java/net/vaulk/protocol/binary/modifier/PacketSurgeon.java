package net.vaulk.protocol.binary.modifier;

import io.netty.buffer.ByteBuf;
import net.vaulk.protocol.binary.ByteBufProtocol;
import net.vaulk.protocol.binary.PacketFlag;

import java.util.UUID;

public final class PacketSurgeon {

    private PacketSurgeon() {}

    public static void setChannelUUID(final ByteBuf packet, final UUID channelUUID) {
        final int metaIdx;

        metaIdx = packet.readerIndex() + ByteBufProtocol.METADATA_OFFSET;
        packet.setLong(metaIdx, channelUUID.getMostSignificantBits());
        packet.setLong(metaIdx + 8, channelUUID.getLeastSignificantBits());
    }

    public static void addFlag(final ByteBuf packet, final PacketFlag flag) {
        final int idx;

        idx = packet.readerIndex() + ByteBufProtocol.FLAGS_OFFSET;
        packet.setByte(idx, PacketFlag.add(packet.getByte(idx), flag));
    }

    public static void removeFlag(final ByteBuf packet, final PacketFlag flag) {
        final int idx;

        idx = packet.readerIndex() + ByteBufProtocol.FLAGS_OFFSET;
        packet.setByte(idx, PacketFlag.remove(packet.getByte(idx), flag));
    }

    public static void setFlags(final ByteBuf packet, final PacketFlag[] flags) {
        packet.setByte(packet.readerIndex() + ByteBufProtocol.FLAGS_OFFSET,
                PacketFlag.combine(flags));
    }

    public static void clearFlags(final ByteBuf packet) {
        packet.setByte(packet.readerIndex() +
                ByteBufProtocol.FLAGS_OFFSET, (byte) 0);
    }
}
