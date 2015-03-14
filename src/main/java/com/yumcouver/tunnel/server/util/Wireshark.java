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

    private String encodeMessageAsBase64(TunnelProto.TunnelCommand tunnelCommand) {
        return encodeMessageAsBase64(tunnelCommand.getMessage().toByteArray());
    }

    private String encodeMessageAsBase64(String message) {
        return encodeMessageAsBase64(message.getBytes());
    }

    public String encodeMessageAsBase64(byte[] bytes) {
        int offset = bytesRemained.length;
        int length = bytes.length + offset;
        byte[] duplicatedBytes = new byte[length];
        System.arraycopy(bytesRemained, 0, duplicatedBytes, 0, offset);
        System.arraycopy(bytes, 0, duplicatedBytes, offset, bytes.length);
        int newBytesRemained = length % 3;
        if(length >= 3) {
            byte[] encodedBytes = new byte[length - newBytesRemained];
            bytesRemained = new byte[newBytesRemained];
            System.arraycopy(duplicatedBytes, 0, encodedBytes, 0,
                    length - newBytesRemained);
            System.arraycopy(duplicatedBytes, length - newBytesRemained,
                    bytesRemained, 0, newBytesRemained);
            return new String(encoder.encode(encodedBytes));
        }
        else {
            bytesRemained = new byte[newBytesRemained];
            System.arraycopy(duplicatedBytes, 0,
                    bytesRemained, 0, newBytesRemained);
            return "";
        }
    }

    public String getMessageContent(TunnelProto.TunnelCommand tunnelCommand) {
        StringBuilder stringBuilder = new StringBuilder();
        String header = "==========================================================\n";
        stringBuilder.append(header);
        stringBuilder.append(String.format(
                "METHOD: %s\n", tunnelCommand.getMethod().getValueDescriptor()));
        if(tunnelCommand.hasSourceId())
            stringBuilder.append(String.format("SOURCE: %s\n",
                    tunnelCommand.getSourceId()));
        else
            stringBuilder.append(String.format("SOURCE: %s\n",
                    tunnelCommand.getSourceType()));
        if(tunnelCommand.hasDestinationId())
            stringBuilder.append(String.format("DESTINATION: %s\n",
                    tunnelCommand.getDestinationId()));
        else
            stringBuilder.append(String.format("DESTINATION: %s\n",
                    tunnelCommand.getDestinationType()));
        if(tunnelCommand.hasSourcePort())
            stringBuilder.append(String.format("SOURCE_PORT: %d\n",
                    tunnelCommand.getSourcePort()));
        if(tunnelCommand.hasDestinationPort())
            stringBuilder.append(String.format("DESTINATION_PORT: %d\n",
                    tunnelCommand.getDestinationPort()));
        if(tunnelCommand.hasMessage()) {
            String encodedMessage = encodeMessageAsBase64(tunnelCommand);
            if(!encodedMessage.isEmpty())
                stringBuilder.append(String.format("MESSAGE: %s\n", encodedMessage));
        }
        stringBuilder.append(header);
        return stringBuilder.toString();
    }
}
