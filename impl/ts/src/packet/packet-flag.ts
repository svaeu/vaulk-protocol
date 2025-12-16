export enum PacketFlag {
    COMPRESSED = 0x01,
    NONE = 0x00
}

export function hasFlag(flags: number, flag: PacketFlag): boolean {
  return (flags & flag) !== 0;
}

export function addFlag(flags: number, flag: PacketFlag): number {
  return flags | flag;
}

export function removeFlag(flags: number, flag: PacketFlag): number {
  return flags & ~flag;
}