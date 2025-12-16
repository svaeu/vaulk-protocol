package net.vaulk.protocol.binary;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.vaulk.protocol.PacketType;
import net.vaulk.protocol.binary.modifier.PacketFactory;

import java.util.EnumSet;
import java.util.UUID;

/**
 * ByteBufProtocol - defines the wire-format and helpers
 * for the binary protocol used by the vaulk server software.
 *
 * <p><b>Wire format (8-byte header, big-endian):</b>
 * <pre>
 *  offset:  0..1    2     3      4     5..7
 *            MAGIC  VER   TYPE   FLAGS  LEN(3)
 * bytes:   [2B]   [1B]  [1B]   [1B]  [3B]   PAYLOAD...
 * </pre>
 *
 * <ul>
 *   <li><b>MAGIC</b> (2 bytes) – constant {@link #MAGIC} (0x5650) to identify frames. Used to quickly reject non-protocol data.</li>
 *   <li><b>VERSION</b> (1 byte) – protocol version. Receivers should check and decide how to treat unknown versions.</li>
 *   <li><b>TYPE</b> (1 byte) – {@link PacketType} type identifier (enum). Values are bitwise-free (each value is an id, not a bitmask).</li>
 *   <li><b>FLAGS</b> (1 byte) – bitmask for single-bit flags (enum) (see {@link PacketFlag}).</li>
 *   <li><b>LEN</b> (3 bytes) – unsigned 24-bit payload length, big-endian. Range: 0 .. {@link #MAX_PAYLOAD_SIZE}.</li>
 *   <li><b>PAYLOAD</b> (LEN bytes) – opaque payload. If LEN == 0, no payload bytes.</li>
 * </ul>
 *
 * <p>All multibyte numeric fields are big-endian (network order). This class intentionally
 * uses {@code getByte/getUnsignedShort} and manual shifts for the 3-byte length field.
 *
 * <p><b>Ownership / ref-count rules</b>:
 * <ul>
 *   <li>{@link #getPayloadSlice(ByteBuf)} returns a {@code slice()} view <em>without</em> changing
 *       reference counts. If the slice must outlive the parent buffer, callers must call
 *       {@code retainedSlice()} / {@code retainedDuplicate()} or otherwise retain the parent buffer.</li>
 *   <li>Factory/writer helpers that compose headers and existing payload buffers must retain
 *       the payload component before handing ownership to a composite buffer (see  {@link PacketFactory}).</li>
 * </ul>
 *
 * <p>It is intended to be used both by encoders and decoders
 * and serve as the single source-of-truth for the wire-schema.
 */
public final class ByteBufProtocol {

    public static final int MAGIC = 0x5650;
    public static final byte VERSION = (byte) 0x01;
    public static final int HEADER_SIZE = 9;

    public static final int MAGIC_OFFSET = 0;
    public static final int VERSION_OFFSET = 2;
    public static final int TYPE_OFFSET = 3;
    public static final int FLAGS_OFFSET = 4;
    public static final int META_LENGTH_OFFSET = 5;
    public static final int PAYLOAD_LENGTH_OFFSET = 6;
    public static final int METADATA_OFFSET = 9;

    public static final int MAX_PAYLOAD_SIZE = 0xFFFFFF;
    public static final int MAX_METADATA_LENGTH = 16;

    private ByteBufProtocol() {}

    /**
     * Validate whether {@code buf} contains a complete, well-formed packet frame (header + payload).
     *
     * <p>Checks performed:
     * <ol>
     *   <li>Readable bytes >= {@link #HEADER_SIZE}.</li>
     *   <li>MAGIC matches {@link #MAGIC}.</li>
     *   <li>Payload length is within {@code 0..MAX_PAYLOAD_SIZE}.</li>
     *   <li>Readable bytes >= {@link #HEADER_SIZE} + payload length (full frame available).</li>
     * </ol>
     *
     * <p><b>Note:</b> This method does not mutate reader/writer indices and does not change ref-counts.
     *
     * @param buf buffer positioned at the packet header (readerIndex points at header start)
     * @return true if {@code buf} contains a complete, valid packet frame
     */
    public static boolean isValidFrame(ByteBuf buf) {
        final int metaLen;
        final int payloadLen;

        metaLen = getMetadataLength(buf);
        if (metaLen < 0 || metaLen > MAX_METADATA_LENGTH) return false;

        payloadLen = getPayloadLength(buf);
        if (payloadLen < 0 || payloadLen > MAX_PAYLOAD_SIZE) return false;

        return buf.readableBytes() >= HEADER_SIZE + metaLen + payloadLen;
    }

    /**
     * Read protocol version from header.
     *
     * @param buf buffer that contains the header at its {@code readerIndex()}
     * @return version byte
     */
    public static byte getPacketVersion(final ByteBuf buf) {
        return buf.getByte(buf.readerIndex() + VERSION_OFFSET);
    }

    /**
     * Read raw packet type byte from header.
     *
     * @param buf buffer that contains the header
     * @return raw packet type (wire byte)
     * @see #toString() for a human readable summary
     */
    public static byte getPacketType(final ByteBuf buf) {
        return buf.getByte(buf.readerIndex() + TYPE_OFFSET);
    }

    /**
     * Read raw flags byte (bitmask) from header.
     *
     * @param buf buffer that contains the header
     * @return raw flags byte (wire bitmask)
     */
    public static byte getFlags(final ByteBuf buf) {
        return buf.getByte(buf.readerIndex() + FLAGS_OFFSET);
    }

    /**
     * Convert raw flags byte into an {@code EnumSet<PacketFlag>}.
     *
     * @param buf buffer that contains the header
     * @return EnumSet of {@code PacketFlag} present in the header (empty if none)
     * @see PacketFlag
     */
    public static EnumSet<PacketFlag> getFlagsSet(ByteBuf buf) {
        return PacketFlag.flagsOf(getFlags(buf));
    }

    public static int getMetadataLength(final ByteBuf buf) {
        return buf.getUnsignedByte(buf.readerIndex() + META_LENGTH_OFFSET);
    }

    /**
     * Read the 3-byte big-endian payload length from the header.
     *
     * <p>Since Java does not have a 3-byte primitive, this method composes the int using shifts.
     *
     * @param buf buffer that contains the header (readerIndex at header start)
     * @return payload length (0..{@link #MAX_PAYLOAD_SIZE})
     */
    public static int getPayloadLength(final ByteBuf buf) {
        final int readerIndex;

        readerIndex = buf.readerIndex() + PAYLOAD_LENGTH_OFFSET;
        return ((buf.getByte(readerIndex) & 0xFF) << 16) |
                ((buf.getByte(readerIndex + 1) & 0xFF) << 8) |
                (buf.getByte(readerIndex + 2) & 0xFF);
    }

    public static ByteBuf getMetadataSlice(final ByteBuf buf) {
        final int metaLen;

        metaLen = getMetadataLength(buf);
        if (metaLen == 0) return Unpooled.EMPTY_BUFFER;

        return buf.slice(buf.readerIndex() + METADATA_OFFSET, metaLen);
    }

    public static UUID getUUIDMetadata(final ByteBuf buf) {
        final int idx;
        final long msb, lsb;

        if (getMetadataLength(buf) != 16) return null;
        idx = buf.readerIndex() + METADATA_OFFSET;

        msb = buf.getLong(idx);
        lsb = buf.getLong(idx + 8);

        if (msb == 0L && lsb == 0L) return null;
        return new UUID(msb, lsb);
    }

//    public static byte getMetadataAsByte(final ByteBuf buf) {
//        if (getMetadataLength(buf) == 0) return 0;
//        return buf.getByte(buf.readerIndex() + METADATA_OFFSET);
//    }

    /**
     * Return a {@code ByteBuf} slice that points to the payload area (no copy).
     *
     * <p><b>Note:</b> This method returns a <em>view</em> (slice) and does NOT change
     * the reference count on the underlying buffer. If the returned slice must outlive the parent
     * buffer, the caller <em>must</em> call {@code retainedSlice()} or otherwise retain the parent
     * buffer to ensure the memory remains valid.
     *
     * @param buf buffer that contains the full frame (readerIndex at header start)
     * @return a slice of the payload region (or {@link Unpooled#EMPTY_BUFFER} if length == 0)
     */
    public static ByteBuf getPayloadSlice(final ByteBuf buf) {
        final int payloadLength;

        payloadLength = getPayloadLength(buf);
        if (payloadLength == 0) return Unpooled.EMPTY_BUFFER;

        return buf.slice(buf.readerIndex() + (METADATA_OFFSET + getMetadataLength(buf)), payloadLength);
    }

    public String toString(final ByteBuf buf) {
        return null;
    }
}
