package com.yumcouver.tunnel.server.util;

import com.yumcouver.tunnel.server.protobuf.TunnelProto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Wireshark {
    private static final Logger LOGGER = LogManager.getLogger(Wireshark.class);
    private static final int DEBUG_MESSAGE_LENGTH = 50;

    public Wireshark() {
    }

    public static String getSubstring(String message) {
        int length = Math.min(message.length(), DEBUG_MESSAGE_LENGTH);
        int index = message.indexOf("\n");
        if(index == -1)
            return message.substring(0, length);
        else
            return message.substring(0, Math.min(index, length));
    }

    public static String log(TunnelProto.TunnelCommand tunnelCommand) {
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
        if(tunnelCommand.hasDestinationIP())
            stringBuilder.append(String.format("DESTINATION_IP: %s\n",
                    tunnelCommand.getDestinationIP()));
        if(tunnelCommand.hasDestinationPort())
            stringBuilder.append(String.format("DESTINATION_PORT: %d\n",
                    tunnelCommand.getDestinationPort()));
        if(tunnelCommand.hasMessage()) {
            String message = tunnelCommand.getMessage().toStringUtf8();
            if(!message.isEmpty()) {
                stringBuilder.append(String.format("MESSAGE: %s\n", getSubstring(message)));
            }
        }
        stringBuilder.append(header);
        return stringBuilder.toString();
    }
}
