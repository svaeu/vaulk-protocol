import type { Packet } from "../packet/packet.js";
import {
    VERSION_OFFSET,
    TYPE_OFFSET,
    FLAGS_OFFSET,
    META_LENGTH_OFFSET,
    PAYLOAD_LENGTH_OFFSET,
    METADATA_OFFSET
} from "../protocol.js";
import { PacketType } from "../packet/packet-type.js";

export function parsePacket(buf: Uint8Array): Packet {
  const version = buf[VERSION_OFFSET];
  const type = buf[TYPE_OFFSET] as PacketType;
  const flags = buf[FLAGS_OFFSET];
  const metaLen = buf[META_LENGTH_OFFSET];
  
  const payloadLen = (buf[PAYLOAD_LENGTH_OFFSET] << 16) |
                     (buf[PAYLOAD_LENGTH_OFFSET + 1] << 8) |
                     buf[PAYLOAD_LENGTH_OFFSET + 2];

  const payloadStart = METADATA_OFFSET + metaLen;
  return {
    version,
    type,
    flags,
    metadata: metaLen > 0 ? buf.subarray(METADATA_OFFSET, METADATA_OFFSET + metaLen) : null,
    payload: buf.subarray(payloadStart, payloadStart + payloadLen)
  };
}