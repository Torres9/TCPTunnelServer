package com.yumcouver.tunnel.server;

import com.yumcouver.tunnel.server.controller.ControllerServer;

import java.io.IOException;

public class TCPTunnelServer {
    public static boolean DEBUG_MODE = true;

    public static void main(String args[]) throws IOException {
        ControllerServer.getInstance();
        System.in.read();
    }
}
