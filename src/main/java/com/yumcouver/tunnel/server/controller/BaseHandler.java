package com.yumcouver.tunnel.server.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BaseHandler {
    protected final Logger LOGGER = LogManager.getLogger(this.getClass());

    protected ControllerServerHandlerAdapter controllerServerHandlerAdapter;

    public BaseHandler(ControllerServerHandlerAdapter controllerServerHandlerAdapter) {
        this.controllerServerHandlerAdapter = controllerServerHandlerAdapter;
    }

    public void shutdown() {
        controllerServerHandlerAdapter.shutdown();
    }

    public void write(byte[] messageBytes) {
        controllerServerHandlerAdapter.write(messageBytes);
    }
}
