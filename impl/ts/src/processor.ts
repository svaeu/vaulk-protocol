import type { Packet } from './packet/packet.js';
import { PacketType } from './packet/packet-type.js';

export function processPacket(
  packet: Packet,
  handlers: Partial<Record<PacketType, (packet: Packet) => void>>
): Boolean {
  const handler = handlers[packet.type];
  if (!handler) { return false; }

  handler(packet);
  return true;
}