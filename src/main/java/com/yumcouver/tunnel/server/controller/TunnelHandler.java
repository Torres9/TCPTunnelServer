package com.yumcouver.tunnel.server.controller;

import com.yumcouver.tunnel.server.forward.ForwardingServerHandlerAdapter;

public class TunnelHandler extends BaseHandler {
    private ForwardingServerHandlerAdapter forwardingServerHandlerAdapter;
    public TunnelHandler(ControllerServerHandlerAdapter controllerServerHandlerAdapter) {
        super(controllerServerHandlerAdapter);
    }

    public void setForwardingServerHandlerAdapter(ForwardingServerHandlerAdapter adapter) {
        this.forwardingServerHandlerAdapter = adapter;
    }

    public void write(byte[] messageBytes) {
        if(forwardingServerHandlerAdapter != null)
            forwardingServerHandlerAdapter.write(messageBytes);
        else
            LOGGER.warn("Forward server handler adapter not found");
    }
}
