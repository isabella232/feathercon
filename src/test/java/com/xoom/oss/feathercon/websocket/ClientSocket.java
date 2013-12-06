package com.xoom.oss.feathercon.websocket;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import java.io.IOException;

@ClientEndpoint
public class ClientSocket {
    public String messageEchoed;
    public volatile boolean spin = true;

    @OnOpen
    public void onWebSocketConnect(Session session) throws IOException, EncodeException {
    }

    @OnMessage
    public void onWebSocketText(String message) throws IOException, EncodeException {
        messageEchoed = message;
        spin = false;
    }

    @OnClose
    public void onWebSocketClose(CloseReason reason) {
    }

    @OnError
    public void onWebSocketError(Throwable cause) {
    }
}
