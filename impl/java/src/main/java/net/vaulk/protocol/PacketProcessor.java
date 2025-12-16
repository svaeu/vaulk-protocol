package net.vaulk.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import net.vaulk.protocol.binary.ByteBufProtocol;

public class PacketProcessor {

    private final PacketExecutor packetExecutor;

    private volatile boolean isAcceptingWork = true;

    public PacketProcessor(final PacketExecutor packetExecutor) {
        this.packetExecutor = packetExecutor;
    }

    public void processSmart(final ByteBuf packet, final Channel channel) {
        final PacketType packetType;

        if (!isAcceptingWork)
            throw new PacketProcessingException(
                    "Processor no longer accepting work."
            );
        if (!isProcessable(packet)) return;

        packetType = PacketType.fromId(ByteBufProtocol.getPacketType(packet));

        if (isRelayPacket(packetType)) {
            packetExecutor.exec(packet, packetType, channel);
            return;
        }

        packet.retain();
        channel.eventLoop().execute(() -> {
            try {
                packetExecutor.exec(packet, packetType, channel);
            } catch (Exception e) {
                channel.pipeline().fireExceptionCaught(e);
            } finally {
                packet.release();
            }
        });
    }

    private boolean isRelayPacket(final PacketType packetType) {
        switch (packetType) {
            case PLACEHOLDER -> {
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private boolean isProcessable(final ByteBuf packet) {
        return packet != null &&
                packet.isReadable() &&
                ByteBufProtocol.isValidFrame(packet);
    }

    public void shutdown() { isAcceptingWork = false; }
}