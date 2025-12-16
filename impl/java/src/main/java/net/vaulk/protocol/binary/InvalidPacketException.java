package net.vaulk.protocol.binary;

public class InvalidPacketException extends RuntimeException {
    public InvalidPacketException(String message) {
        super(message);
    }
}
