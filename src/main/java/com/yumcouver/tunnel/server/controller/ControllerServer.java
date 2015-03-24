package com.yumcouver.tunnel.server.controller;

import com.yumcouver.tunnel.server.util.ConfigReader;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ControllerServer {
    public static String DELIMITER = "-";

    private static final Logger LOGGER = LogManager.getLogger(ControllerServer.class);
    private static ControllerServer ourInstance = new ControllerServer();

    private final EventLoopGroup bossGroup = new NioEventLoopGroup();
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();

    public static ControllerServer getInstance() {
        return ourInstance;
    }

    private ControllerServer() {
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap(); // (2)
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ControllerServerHandlerAdapter());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            serverBootstrap.bind(ConfigReader.TCP_TUNNEL_SERVER_PORT).sync();
            LOGGER.info("Listening on 0.0.0.0:{}", ConfigReader.TCP_TUNNEL_SERVER_PORT);
        } catch (InterruptedException e) {
            LOGGER.catching(e);
        }
    }

    public void shutdown() {
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }
}
