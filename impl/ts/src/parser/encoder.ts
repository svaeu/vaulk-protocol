import { PacketType } from "../packet/packet-type.js";
import {
  MAGIC,
  VERSION,
  HEADER_SIZE,
  MAGIC_OFFSET,
  VERSION_OFFSET,
  TYPE_OFFSET,
  FLAGS_OFFSET,
  META_LENGTH_OFFSET,
  PAYLOAD_LENGTH_OFFSET,
  METADATA_OFFSET,
  MAX_PAYLOAD_SIZE,
  MAX_METADATA_LENGTH
} from "../protocol.js";
import { PacketFlag } from "../packet/packet-flag.js";

export function createPacket(
  type: PacketType,
  payload: Uint8Array,
  flags: number = PacketFlag.NONE,
  metadata: Uint8Array | null = null
): Uint8Array {
  const metaLen = metadata ? Math.min(metadata.length, MAX_METADATA_LENGTH) : 0;
  const payloadLen = Math.min(payload.length, MAX_PAYLOAD_SIZE);
  const totalLen = HEADER_SIZE + metaLen + payloadLen;

  const buf = new Uint8Array(totalLen);

  buf[MAGIC_OFFSET] = (MAGIC >> 8) & 0xFF;
  buf[MAGIC_OFFSET + 1] = MAGIC & 0xFF;

  buf[VERSION_OFFSET] = VERSION;

  buf[TYPE_OFFSET] = type;

  buf[FLAGS_OFFSET] = flags;

  buf[META_LENGTH_OFFSET] = metaLen;

  buf[PAYLOAD_LENGTH_OFFSET] = (payloadLen >> 16) & 0xFF;
  buf[PAYLOAD_LENGTH_OFFSET + 1] = (payloadLen >> 8) & 0xFF;
  buf[PAYLOAD_LENGTH_OFFSET + 2] = payloadLen & 0xFF;

  if (metadata && metaLen > 0) {
    buf.set(metadata.subarray(0, metaLen), METADATA_OFFSET);
  }

  buf.set(payload.subarray(0, payloadLen), METADATA_OFFSET + metaLen);

  return buf;
}

function concatenate(a: Uint8Array, b: Uint8Array): Uint8Array {
  const result = new Uint8Array(a.length + b.length);
  result.set(a, 0);
  result.set(b, a.length);
  return result;
}