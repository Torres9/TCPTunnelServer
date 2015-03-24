package com.yumcouver.tunnel.server.forward;

import com.yumcouver.tunnel.server.TCPTunnelServer;
import com.yumcouver.tunnel.server.controller.ControllerHandler;
import com.yumcouver.tunnel.server.controller.TunnelHandler;
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

public class ForwardingServerHandlerAdapter extends ChannelInboundHandlerAdapter {
    private static Logger LOGGER = LogManager.getLogger(ForwardingServerHandlerAdapter.class);

    private ChannelHandlerContext ctx;
    private TunnelHandler tunnelHandler;
    private ControllerHandler controllerHandler;

    public ForwardingServerHandlerAdapter(ControllerHandler controllerHandler) {
        this.controllerHandler = controllerHandler;
    }

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
        controllerHandler.sendSYN();
        while(tunnelHandler == null)
            tunnelHandler = controllerHandler.getTunnelHandler();
        tunnelHandler.setForwardingServerHandlerAdapter(this);
        LOGGER.info("{}:{} connected", inetSocketAddress.getHostString(),
                inetSocketAddress.getPort());
        if(controllerHandler != null)
            controllerHandler.shutdown();
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
        tunnelHandler.shutdown();
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
        tunnelHandler.write(messageBytes);
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
}
