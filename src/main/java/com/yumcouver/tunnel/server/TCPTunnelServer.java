package com.yumcouver.tunnel.server;

//import com.yumcouver.tunnel.server.websocket.TCPTunnelServerEndpoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.server.ResourceConfig;
//import org.glassfish.tyrus.server.Server;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/")
public class TCPTunnelServer extends ResourceConfig {
    private static final Logger LOGGER = LogManager.getLogger(TCPTunnelServer.class);
    public static final boolean DEBUG_MODE = true;

    public TCPTunnelServer() {
        packages("com.yumcouver.tunnel.server.websocket");
        packages("com.yumcouver.tunnel.server.rest");
    }

//    public static void main(String[] args) throws Exception {

//        URI baseUri = UriBuilder.fromUri("http://localhost/").port(8080).build();
//        ResourceConfig resourceConfig = new TCPTunnelServer();
//        HttpServer server = JdkHttpServerFactory.createHttpServer(baseUri, resourceConfig);
//
//        System.out.println("Server running");
//        System.out.println("Visit: http://localhost:8080/");
//        System.out.println("Hit return to stop...");
//        System.in.read();
//        System.out.println("Stopping server");
//        server.stop(0);
//        System.out.println("Server stopped");

//        Server server = new Server("localhost", 8080, "/", null, TCPTunnelServerEndpoint.class);
//        server.start();
//        System.in.read();
//        server.stop();
//    }
}
