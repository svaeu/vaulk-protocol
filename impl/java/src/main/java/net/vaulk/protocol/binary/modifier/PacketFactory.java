package net.vaulk.protocol.binary.modifier;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import net.vaulk.protocol.binary.InvalidPacketException;

import static net.vaulk.protocol.binary.ByteBufProtocol.*;

public final class PacketFactory {

    public static ByteBuf createPacket(final byte packetType,
                                       final byte flags,
                                       final byte[] metadata,
                                       final byte[] payload) {
        final int metaLen, payloadLen;
        final ByteBuf packet;

        metaLen = (metadata != null) ? metadata.length : 0;
        payloadLen = (payload != null) ? payload.length : 0;

        if (metaLen > MAX_METADATA_LENGTH)
            throw new InvalidPacketException("Metadata overflow: " + metaLen);

        if (payloadLen > MAX_PAYLOAD_SIZE)
            throw new InvalidPacketException("Payload overflow: " + payloadLen);

        packet = PooledByteBufAllocator.DEFAULT.buffer
                (HEADER_SIZE + metaLen + payloadLen);
        try {
            writeHeader(packet, packetType, flags, metaLen, payloadLen);

            if (metaLen > 0)
                packet.writeBytes(metadata);

            if (payloadLen > 0)
                packet.writeBytes(payload);

            return packet;
        } catch (Throwable t) {
            packet.release();
            throw t;
        }
    }

    private static void writeHeader(final ByteBuf dst,
                                    final byte packetType,
                                    final byte flags,
                                    final int metaLen,
                                    final int payloadLen) {
        dst.writeShort(MAGIC);
        dst.writeByte(VERSION);
        dst.writeByte(packetType);
        dst.writeByte(flags);
        dst.writeByte(metaLen);
        dst.writeByte((payloadLen >> 16) & 0xFF);
        dst.writeByte((payloadLen >> 8) & 0xFF);
        dst.writeByte(payloadLen & 0xFF);
    }
}