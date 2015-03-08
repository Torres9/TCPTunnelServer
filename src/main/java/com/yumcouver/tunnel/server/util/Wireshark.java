package com.yumcouver.tunnel.server.util;

import com.yumcouver.tunnel.server.protobuf.TunnelProto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Base64;

public class Wireshark {
    private static final Logger LOGGER = LogManager.getLogger(Wireshark.class);

    private final Base64.Encoder encoder = Base64.getEncoder();
    private byte[] bytesRemained = new byte[0];

    public Wireshark() {
    }

    public String encodeMessageAsBase64(TunnelProto.TunnelCommand tunnelCommand) {
        int offset = bytesRemained.length;
        int length = tunnelCommand.getMessage().size() + offset;
        int newBytesRemained = length % 3;
        byte[] duplicatedBytes = new byte[length-newBytesRemained];
        System.arraycopy(bytesRemained, 0, duplicatedBytes, 0, offset);
        tunnelCommand.getMessage().copyTo(duplicatedBytes, 0, offset,
                duplicatedBytes.length - offset);
        bytesRemained = new byte[newBytesRemained];
        tunnelCommand.getMessage().copyTo(bytesRemained,
                duplicatedBytes.length - offset, 0, newBytesRemained);
        return new String(encoder.encode(duplicatedBytes));
    }
}
