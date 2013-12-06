package com.xoom.oss.feathercon.websocket;

import com.xoom.oss.feathercon.WebSocketEndpointConfiguration;
import org.junit.Before;
import org.junit.Test;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class WebSocketEndpointConfigurationBuilderTest {
    private WebSocketEndpointConfiguration.Builder builder;

    @Before
    public void setUp() throws Exception {
        builder = new WebSocketEndpointConfiguration.Builder();
    }

    @Test
    public void testWithEndpointClass() throws Exception {
        builder.withEndpointClass(EndPoint.class);
    }

    @Test
    public void testBuild() throws Exception {
        builder.withEndpointClass(EndPoint.class);
        WebSocketEndpointConfiguration webSocketEndpointConfiguration = builder.build();
        assertThat(webSocketEndpointConfiguration.endpointClasses.contains(EndPoint.class), equalTo(true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoClassAnnotation() throws Exception {
        builder.withEndpointClass(EndPointNoAnnotation.class);
    }

    @ServerEndpoint(value = "/events/")
    private class EndPoint {
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

    private class EndPointNoAnnotation {
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
