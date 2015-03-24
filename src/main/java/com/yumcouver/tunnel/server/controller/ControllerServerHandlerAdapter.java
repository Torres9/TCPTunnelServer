package com.yumcouver.tunnel.server.controller;

import com.google.protobuf.InvalidProtocolBufferException;
import com.yumcouver.tunnel.server.TCPTunnelServer;
import com.yumcouver.tunnel.server.forward.ForwardingServer;
import com.yumcouver.tunnel.server.protobuf.TunnelProto;
import com.yumcouver.tunnel.server.util.Wireshark;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;

public class ControllerServerHandlerAdapter extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER =
            LogManager.getLogger(ControllerServerHandlerAdapter.class);
    private BaseHandler baseHandler = null;

    private ChannelHandlerContext ctx;

    public void write(byte[] messageBytes) {
        synchronized (this) {
            if (ctx != null) {
                final ByteBuf messageByteBuf = Unpooled.copiedBuffer(messageBytes);
                ctx.write(messageByteBuf);
                ctx.flush();
                if (TCPTunnelServer.DEBUG_MODE)
                    LOGGER.debug("Sent message: {}", Wireshark.getSubstring(messageBytes));
            } else
                LOGGER.warn("Not connected");
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        synchronized (this) {
            this.ctx = ctx;
        }
        InetSocketAddress inetSocketAddress =
                (InetSocketAddress) ctx.channel().remoteAddress();
        LOGGER.info("{}:{} connected", inetSocketAddress.getHostString(),
                inetSocketAddress.getPort());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        synchronized (this) {
            this.ctx = null;
        }
        InetSocketAddress inetSocketAddress =
                (InetSocketAddress) ctx.channel().remoteAddress();
        LOGGER.info("{}:{} disconnected", inetSocketAddress.getHostString(),
                inetSocketAddress.getPort());
    }

    private void sendSYN() {
        TunnelProto.TunnelCommand tunnelCommand = TunnelProto.TunnelCommand.newBuilder()
                .setMethod(TunnelProto.TunnelCommand.Method.SYN)
                .build();
        write(tunnelCommand.toByteArray());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        final ByteBuf messageByteBuf = (ByteBuf) msg;
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        while (messageByteBuf.isReadable())
            byteArrayOutputStream.write(messageByteBuf.readByte());
        final byte[] messageBytes = byteArrayOutputStream.toByteArray();
        if (TCPTunnelServer.DEBUG_MODE)
            LOGGER.debug("Received message: {}", Wireshark.getSubstring(messageBytes));
        if (baseHandler == null)
            try {
                TunnelProto.TunnelCommand tunnelCommand =
                        TunnelProto.TunnelCommand.parseFrom(messageBytes);
                switch (tunnelCommand.getMethod()) {
                    case CONTROLLER_INIT:
                        baseHandler = new ControllerHandler(this);
                        ForwardingServer forwardingServer = new ForwardingServer((ControllerHandler) baseHandler);
                        ((ControllerHandler) baseHandler).sendControllerId(forwardingServer.getPort());
                        break;
                    case TUNNEL_INIT:
                        baseHandler = new TunnelHandler(this);
                        String controllerId =
                                tunnelCommand.getMessage().split(ControllerServer.DELIMITER)[0];
                        ControllerHandler.addTunnelHanlder(controllerId, (TunnelHandler) baseHandler);
                        break;
                    default:
                        LOGGER.warn("method unrecognized");
                }
            } catch (InvalidProtocolBufferException e) {
                LOGGER.catching(e);
            }
        else if (baseHandler instanceof TunnelHandler) {
            ((TunnelHandler) baseHandler).forward(messageBytes);
        }
        ReferenceCountUtil.release(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        synchronized (this) {
            this.ctx = null;
        }
        LOGGER.catching(cause);
        ctx.close();
    }

    public void shutdown() {
        synchronized (this) {
            if (this.ctx != null)
                ctx.close();
        }
    }

    public boolean isConnected() {
        synchronized (this) {
            return this.ctx != null;
        }
    }
}
