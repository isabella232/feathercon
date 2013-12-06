package com.xoom.oss.feathercon;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@ClientEndpoint
public class ClientSocket {
    Set<Session> sessions = new HashSet<Session>();
    public String messageEchoed;

    @OnOpen
    public void onWebSocketConnect(Session session) throws IOException, EncodeException {
        sessions.add(session);
    }

    @OnMessage
    public void onWebSocketText(String message) throws IOException, EncodeException {
        System.out.printf("Client received message %s\n", message);
        messageEchoed = message;
    }

    @OnClose
    public void onWebSocketClose(CloseReason reason) {
//        System.out.println("@@@ OnClose");
    }

    @OnError
    public void onWebSocketError(Throwable cause) {
//        System.out.println("@@@ OnError");
    }
}
