package com.yumcouver.tunnel.server.util;

public class Wireshark {
    private static final int DEBUG_MESSAGE_LENGTH = 50;

    public static String getSubstring(String message) {
        int length = Math.min(message.length(), DEBUG_MESSAGE_LENGTH);
        int index = message.indexOf("\n");
        if (index == -1)
            return message.substring(0, length);
        else
            return message.substring(0, Math.min(index, length));
    }

    public static String getSubstring(byte[] message) {
        return getSubstring(new String(message));
    }
}
