package com.xoom.oss.feathercon.websocket;

import com.xoom.oss.feathercon.FeatherCon;
import com.xoom.oss.feathercon.WebSocketEndpointConfiguration;
import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.net.URI;

public class WebSocketTestWithStaticEndpointClass {

    private FeatherCon server;

    @Before
    public void setup() throws Exception {
        WebSocketEndpointConfiguration.Builder wsb = new WebSocketEndpointConfiguration.Builder();
        WebSocketEndpointConfiguration wsconfig = wsb.withEndpointClass(ServerSocket.class).build();
        FeatherCon.Builder serverBuilder = new FeatherCon.Builder().withPort(0);
        server = serverBuilder.withWebSocketConfiguration(wsconfig).build();
        server.start();
    }

    @After
    public void shutdown() throws Exception {
        server.stop();
    }

    @Test
    public void testSocket() throws Exception {
        ClientSocket clientSocket = new ClientSocket();
        String message = "Hello";

        URI uri = URI.create(String.format("ws://localhost:%d/events", server.getHttpPort()));
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            try {
                Session session = container.connectToServer(clientSocket, uri);
                session.getBasicRemote().sendText(message);
                while (clientSocket.spin) ;
                session.close();
            } finally {
                // Force lifecycle stop when done with container.
                // This is to free up threads and resources that the
                // JSR-356 container allocates. But unfortunately
                // the JSR-356 spec does not handle lifecycles (yet)
                if (container instanceof LifeCycle) {
                    ((LifeCycle) container).stop();
                }
            }
        } catch (Throwable t) {
            t.printStackTrace(System.err);
        }

        Assert.assertEquals(clientSocket.messageEchoed, "echo:" + message);
    }
}