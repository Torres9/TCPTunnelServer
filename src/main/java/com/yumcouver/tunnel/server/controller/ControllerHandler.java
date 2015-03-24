package com.yumcouver.tunnel.server.controller;

import com.yumcouver.tunnel.server.forward.ForwardingServer;
import com.yumcouver.tunnel.server.protobuf.TunnelProto;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class ControllerHandler extends BaseHandler {
    private final static int POOL_CAPACITY = 5;
    private static int count = 0;
    private ForwardingServer forwardingServer;

    private static Map<Integer, ControllerHandler> idControllerHandlerMappings =
            new ConcurrentHashMap<>();

    private final int controllerId;
    private final ArrayBlockingQueue<TunnelHandler> tunnelHandlerPool =
            new ArrayBlockingQueue<>(POOL_CAPACITY);

    public void addTunnelHandler(TunnelHandler tunnelHandler) {
        try {
            tunnelHandlerPool.put(tunnelHandler);
        } catch (InterruptedException e) {
            LOGGER.catching(e);
        }
    }

    public static void addTunnelHandler(String controllerId, TunnelHandler tunnelHandler) {
        ControllerHandler controllerHandler =
                idControllerHandlerMappings.get(Integer.parseInt(controllerId));
        if (controllerHandler != null)
            controllerHandler.addTunnelHandler(tunnelHandler);
    }

    public ControllerHandler(ControllerServerHandlerAdapter controllerServerHandlerAdapter) {
        super(controllerServerHandlerAdapter);
        synchronized (ControllerHandler.class) {
            controllerId = count;
            count++;
        }
        idControllerHandlerMappings.put(controllerId, this);
    }

    public void sendControllerId() {
        TunnelProto.TunnelCommand tunnelCommand = TunnelProto.TunnelCommand.newBuilder()
                .setMethod(TunnelProto.TunnelCommand.Method.CONTROLLER_INIT)
                .setMessage(String.valueOf(controllerId + ControllerServer.DELIMITER +
                        forwardingServer.getPort()))
                .build();
        controllerServerHandlerAdapter.write(tunnelCommand.toByteArray());
    }

    public TunnelHandler getTunnelHandler() {
        try {
            return tunnelHandlerPool.take();
        } catch (InterruptedException e) {
            LOGGER.catching(e);
            return null;
        }
    }

    public void sendSYN() {
        TunnelProto.TunnelCommand tunnelCommand = TunnelProto.TunnelCommand.newBuilder()
                .setMethod(TunnelProto.TunnelCommand.Method.SYN)
                .build();
        controllerServerHandlerAdapter.write(tunnelCommand.toByteArray());
    }

    public void setForwardingServer(ForwardingServer forwardingServer) {
        assert this.forwardingServer == null;
        this.forwardingServer = forwardingServer;
    }

    @Override
    public void shutdown() {
        controllerServerHandlerAdapter.shutdown();
        forwardingServer.shutdown();
    }
}
