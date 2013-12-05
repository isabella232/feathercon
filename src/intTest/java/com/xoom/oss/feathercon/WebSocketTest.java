package com.xoom.oss.feathercon;

import org.junit.Test;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

public class WebSocketTest {
    @Test
    public void testWebSocketSetup() throws Exception {
        FeatherCon.Builder serverBuilder = new FeatherCon.Builder();
        WebSocketEndpointConfiguration.Builder wsb = new WebSocketEndpointConfiguration.Builder();
        WebSocketEndpointConfiguration wsconfig = wsb.withEndpointClass(MyEndpoint.class).build();
        FeatherCon server = serverBuilder.withWebSocketConfiguration(wsconfig).build();
        server.start();
        server.join();
    }

    @ServerEndpoint(value = "/events/")
    private class MyEndpoint {
        @OnOpen
        public void onWebSocketConnect(Session sess) {
        }

        @OnMessage
        public void onWebSocketText(String message) {
        }

        @OnClose
        public void onWebSocketClose(CloseReason reason) {
        }

        @OnError
        public void onWebSocketError(Throwable cause) {
        }
    }
}
