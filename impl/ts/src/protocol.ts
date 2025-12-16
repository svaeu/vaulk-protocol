export const MAGIC = 0x5650;
export const VERSION = 0x01;
export const HEADER_SIZE = 9;

export const MAGIC_OFFSET = 0;
export const VERSION_OFFSET = 2;
export const TYPE_OFFSET = 3;
export const FLAGS_OFFSET = 4;
export const META_LENGTH_OFFSET = 5;
export const PAYLOAD_LENGTH_OFFSET = 6;
export const METADATA_OFFSET = 9;

export const MAX_PAYLOAD_SIZE = 0xFFFFFF;
export const MAX_METADATA_LENGTH = 16;

export function isValidFrame(buf: Uint8Array): boolean {
  if (buf.length < HEADER_SIZE) return false;

  const magic = (buf[MAGIC_OFFSET] << 8) | buf[MAGIC_OFFSET + 1];
  if (magic !== MAGIC) return false;

  const metaLen = buf[META_LENGTH_OFFSET];
  if (metaLen > MAX_METADATA_LENGTH) return false;

  const payloadLen = (buf[PAYLOAD_LENGTH_OFFSET] << 16) |
                     (buf[PAYLOAD_LENGTH_OFFSET + 1] << 8) |
                     buf[PAYLOAD_LENGTH_OFFSET + 2];
  
  if (payloadLen > MAX_PAYLOAD_SIZE) return false;

  return buf.length >= HEADER_SIZE + metaLen + payloadLen;
}