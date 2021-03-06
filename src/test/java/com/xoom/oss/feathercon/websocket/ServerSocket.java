package com.xoom.oss.feathercon.websocket;

import javax.websocket.CloseReason;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@ServerEndpoint(value = "/events")
public class ServerSocket {
    Set<Session> sessions = new HashSet<Session>();

    @OnOpen
    public void onWebSocketConnect(Session session) throws IOException, EncodeException {
        sessions.add(session);
    }

    @OnMessage
    public void onWebSocketText(String message) throws IOException, EncodeException {
        for (final Session session : sessions) {
            session.getBasicRemote().sendObject(String.format("echo:%s", message));
        }
    }

    @OnClose
    public void onWebSocketClose(CloseReason reason) {
    }

    @OnError
    public void onWebSocketError(Throwable cause) {
    }
}
