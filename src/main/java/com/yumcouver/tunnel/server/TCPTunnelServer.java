package com.yumcouver.tunnel.server;

import com.yumcouver.tunnel.server.controller.ControllerServer;

public class TCPTunnelServer {
    public static boolean DEBUG_MODE = false;

    public static void main(String args[]) throws Exception {
        ControllerServer.getInstance();
        System.in.read();
        ControllerServer.getInstance().shutdown();
        ControllerServer.getInstance().join();
    }
}
