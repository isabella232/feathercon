package com.xoom.oss.feathercon;

import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.net.URI;

public class WebSocketTest {

    private FeatherCon server;

    @Before
    public void setup() throws Exception {

        ServletConfiguration.Builder servletConfig = new ServletConfiguration.Builder();
        servletConfig.withServletClass(DefaultServlet.class).withPathSpec("/*").withInitParameter("resourceBase", "src/intTest/html");
        ServletConfiguration servlet = servletConfig.build();

        WebSocketEndpointConfiguration.Builder wsb = new WebSocketEndpointConfiguration.Builder();
        WebSocketEndpointConfiguration wsconfig = wsb.withEndpointClass(ServerSocket.class).build();

        FeatherCon.Builder serverBuilder = new FeatherCon.Builder();
        server = serverBuilder.withWebSocketConfiguration(wsconfig).withServletConfiguration(servlet).build();

        // Start the server, open your browser's Javascript debug console, point your browser to http://localhost:8080/index.html,
        // and look for console messages reported by the WebSocket client.

        server.start();
        Thread.sleep(2000);
    }

    @After
    public void shutdown() throws Exception {
        server.stop();
    }

    @Test
    public void testSocket() throws Exception {
        ClientSocket clientSocket = new ClientSocket();
        String message = "Hello";

        URI uri = URI.create("ws://localhost:8080/events/");
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            try {
                // Attempt Connect
                Session session = container.connectToServer(clientSocket, uri);
                // Send a message
                session.getBasicRemote().sendText(message);
                // Close session
                Thread.sleep(2000);
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