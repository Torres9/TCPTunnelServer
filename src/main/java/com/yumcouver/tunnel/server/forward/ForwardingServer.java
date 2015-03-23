package com.yumcouver.tunnel.server.forward;

import com.yumcouver.tunnel.server.controller.ControllerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

public class ForwardingServer {
    private static final Logger LOGGER = LogManager.getLogger(ForwardingServer.class);
    private static final Random random = new Random();

    private final EventLoopGroup bossGroup = new NioEventLoopGroup();
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private int port;

    public ForwardingServer(final ControllerHandler controllerHandler) {
        boolean inUse = true;
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(
                                new ForwardingServerHandlerAdapter(controllerHandler));
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        while(inUse) {
            port = generatePort();
            try {
                serverBootstrap.bind(port).sync();
                inUse = false;
            } catch (Exception e) {
                inUse = true;
            }
        }
        LOGGER.info("Listening on port: {}", port);
    }

    private int generatePort() {
        return random.nextInt(20000) + 30000;
    }

    public void shutdown() {
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

    public int getPort() {
        return port;
    }
}
