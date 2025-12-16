import { PacketType } from "./packet-type.js";

export interface Packet {
  version: number;
  type: PacketType;
  flags: number;
  metadata: Uint8Array | null;
  payload: Uint8Array;
}