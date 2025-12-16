package net.vaulk.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

public abstract class PacketExecutor {

    protected abstract void handlePlaceholder(byte[] payload, Channel ch);

    public final void exec(final ByteBuf packet,
                           final PacketType packetType,
                           final Channel session) {
        switch(packetType) {

        }
    }
}
