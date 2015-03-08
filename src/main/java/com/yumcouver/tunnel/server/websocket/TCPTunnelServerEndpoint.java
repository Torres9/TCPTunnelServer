package com.yumcouver.tunnel.server.websocket;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.yumcouver.tunnel.server.TCPTunnelServer;
import com.yumcouver.tunnel.server.protobuf.TunnelProto;
import com.yumcouver.tunnel.server.util.Wireshark;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/v1/tunnel")
public class TCPTunnelServerEndpoint {
    private static final Logger LOGGER =
            LogManager.getLogger(TCPTunnelServerEndpoint.class);
    private static final String SEVER_ID = "SERVER-0000-0000-0000-000000000000";
    private static final String UNKOWN_ID = "UNKOWN-FFFF-FFFF-FFFF-FFFFFFFFFFFF";

    private static final Map<String, TCPTunnelServerEndpoint> idConnectionsMappings =
            new ConcurrentHashMap<>();

    private final Wireshark writeStreamWireshark = new Wireshark();
    private final Wireshark readStreamWireshark = new Wireshark();
    private Session session;
    private String prefixOfSessionId;

    private boolean isIdValid(String sessionId) {
        return sessionId != null && idConnectionsMappings.containsKey(sessionId);
    }

    @OnMessage
    public synchronized void onMessage(byte[] request) throws IOException {
        try {
            TunnelProto.TunnelCommand tunnelCommand =
                    TunnelProto.TunnelCommand.parseFrom(request);

            LOGGER.info("Received message from {}, METHOD: {}",
                    prefixOfSessionId,
                    tunnelCommand.getMethod().getValueDescriptor());
            if(TCPTunnelServer.DEBUG_MODE)
                LOGGER.debug("Received message content: {}",
                        readStreamWireshark.encodeMessageAsBase64(tunnelCommand));

            String destinationId = tunnelCommand.getDestinationId();
            String sourceId = tunnelCommand.getSourceId();
            switch (tunnelCommand.getMethod()) {
                case ID:
                    send(sessionIdResponse());
                    break;
                case SEND: case ACK:
                    if(!isIdValid(destinationId))
                        send(errorMessage("Invalid destination id"));
                    else if(!isIdValid(sourceId))
                        send(errorMessage("Invalid source id"));
                    else if(tunnelCommand.getMethod() ==
                            TunnelProto.TunnelCommand.Method.SEND
                            && (tunnelCommand.getSourcePort() <= 0
                            || tunnelCommand.getSourcePort() >= 65536
                            || tunnelCommand.getDestinationPort() <= 0
                            || tunnelCommand.getDestinationPort() >= 65536))
                        send(errorMessage("Invalid port number"));
                    else
                        idConnectionsMappings.get(destinationId).send(tunnelCommand);
                    break;
                case ERROR:
                    if(tunnelCommand.getDestinationType() == TunnelProto.TunnelCommand.EndType.SERVER)
                        LOGGER.error(tunnelCommand.getMessage());
                    else if(isIdValid(destinationId) && isIdValid(sourceId))
                        idConnectionsMappings.get(destinationId).send(tunnelCommand);
                    break;
                // TODO add close method
                default:
                    break;
            }
        } catch (InvalidProtocolBufferException e) {
            LOGGER.catching(e);
        }
    }

    private TunnelProto.TunnelCommand errorMessage(String errorCode)
            throws IOException {
        return TunnelProto.TunnelCommand.newBuilder()
                .setMethod(TunnelProto.TunnelCommand.Method.ERROR)
                .setSourceType(TunnelProto.TunnelCommand.EndType.SERVER)
                .setDestinationType(TunnelProto.TunnelCommand.EndType.CLIENT)
                .setMessage(ByteString.copyFromUtf8(errorCode))
                .build();
    }

    public synchronized void send(TunnelProto.TunnelCommand tunnelCommand) throws IOException {
        OutputStream outputStream = session.getBasicRemote().getSendStream();
        tunnelCommand.writeTo(outputStream);
        outputStream.flush();
        outputStream.close();
        LOGGER.info("Sent message to {}, METHOD: {}", prefixOfSessionId,
                tunnelCommand.getMethod().getValueDescriptor());
        if(TCPTunnelServer.DEBUG_MODE)
            LOGGER.debug("Sent message content: {}",
                    writeStreamWireshark.encodeMessageAsBase64(tunnelCommand));
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        prefixOfSessionId = session.getId().split("-")[0];
        idConnectionsMappings.put(session.getId(), this);
        LOGGER.info("{} connected", prefixOfSessionId);
    }

    @OnError
    public void onError(Throwable t) {
        idConnectionsMappings.remove(session.getId());
        LOGGER.catching(t);
    }

    @OnClose
    public void onClose(CloseReason reason) {
        idConnectionsMappings.remove(session.getId());
        LOGGER.info("Session {} closed, reason: {}", prefixOfSessionId, reason.getCloseCode());
    }

    private TunnelProto.TunnelCommand sessionIdResponse() {
        return TunnelProto.TunnelCommand.newBuilder()
                .setMethod(TunnelProto.TunnelCommand.Method.ID)
                .setSourceType(TunnelProto.TunnelCommand.EndType.SERVER)
                .setDestinationType(TunnelProto.TunnelCommand.EndType.CLIENT)
                .setMessage(ByteString.copyFromUtf8(session.getId()))
                .build();
    }
}
