package com.xoom.oss.feathercon;

import org.eclipse.jetty.servlet.DefaultServlet;
import org.junit.Test;

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

public class WebSocketTest {
    @Test
    public void testWebSocketSetup() throws Exception {
        FeatherCon.Builder serverBuilder = new FeatherCon.Builder();

        ServletConfiguration.Builder servletConfig = new ServletConfiguration.Builder();
        servletConfig.withServletClass(DefaultServlet.class).withPathSpec("/*").withInitParameter("resourceBase", "src/intTest/html");
        ServletConfiguration servlet = servletConfig.build();

        WebSocketEndpointConfiguration.Builder wsb = new WebSocketEndpointConfiguration.Builder();
        WebSocketEndpointConfiguration wsconfig = wsb.withEndpointClass(MyEndpoint.class).build();

        FeatherCon server = serverBuilder.withWebSocketConfiguration(wsconfig).withServletConfiguration(servlet).build();

        server.start();
        server.join();
    }

    @ServerEndpoint(value = "/events/")
    public static class MyEndpoint {
        Set<Session> sessions = new HashSet<Session>();

        @OnOpen
        public void onWebSocketConnect(Session session) throws IOException, EncodeException {
            sessions.add(session);
        }

        @OnMessage
        public void onWebSocketText(String message) throws IOException, EncodeException {
            for (final Session session : sessions) {
                session.getBasicRemote().sendObject(String.format("echo: %s", message));
            }
        }

        @OnClose
        public void onWebSocketClose(CloseReason reason) {
            System.out.println("@@@ C");
        }

        @OnError
        public void onWebSocketError(Throwable cause) {
            System.out.println("@@@ D");
        }
    }
}
