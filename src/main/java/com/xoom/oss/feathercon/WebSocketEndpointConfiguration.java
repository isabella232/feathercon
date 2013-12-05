package com.xoom.oss.feathercon;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.server.ServerEndpoint;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class WebSocketEndpointConfiguration {

    public final Collection<Class> endpointClasses;

    private WebSocketEndpointConfiguration(Collection<Class> endpointClasses) {
        this.endpointClasses = Collections.unmodifiableCollection(endpointClasses);
    }

    public static class Builder {
        private boolean built;
        Collection<Class> endpointClasses = new ArrayList<Class>();

        public Builder withEndpointClass(Class endpointClass) {
            requireServerEndPointAnnotation(endpointClass);
            requireHandlerMethods(endpointClass);
            endpointClasses.add(endpointClass);
            return this;
        }

        public WebSocketEndpointConfiguration build() {
            if (built) {
                throw new IllegalStateException("This builder can be used to produce one configuration instance.  Please create a new builder.");
            }
            built = true;
            return new WebSocketEndpointConfiguration(endpointClasses);
        }

        // file bug:  Option-return at EOL should offer to Iterate, like it does for Colelctions
        private void requireHandlerMethods(Class endpointClass) {
            boolean hasOnOpenAnnotation = false;
            boolean hasOnMessageAnnotation = false;
            boolean hasOnCloseAnnotation = false;
            boolean hasOnErrorAnnotation = false;

            Method[] methods = endpointClass.getMethods();
            for (Method method : methods) {
                Annotation[] annotations = method.getAnnotations();
                for (Annotation annotation : annotations) {
                    if (annotation instanceof OnOpen) {
                        hasOnOpenAnnotation = true;
                        continue;
                    }
                    if (annotation instanceof OnClose) {
                        hasOnCloseAnnotation = true;
                        continue;
                    }
                    if (annotation instanceof OnMessage) {
                        hasOnMessageAnnotation = true;
                        continue;
                    }
                    if (annotation instanceof OnError) {
                        hasOnErrorAnnotation = true;
                        continue;
                    }
                }
            }
            if (!(hasOnOpenAnnotation && hasOnMessageAnnotation && hasOnCloseAnnotation && hasOnErrorAnnotation)) {
                throw new IllegalArgumentException(String.format("Class %s must have methods annotated with OnOpen, OnClose, OnMessage, and OnError javax.websocket annotations", endpointClass));
            }
        }

        private void requireServerEndPointAnnotation(Class c) {
            Annotation annotation = c.getAnnotation(ServerEndpoint.class);
            if (annotation == null) {
                throw new IllegalArgumentException(String.format("Endpoint class must be annotated with javax.websocket.server.ServerEndpoint"));
            }
        }
    }

}

/*
@ServerEndpoint(value = "/events/")
public class EventSocket {
    @OnOpen
    public void onWebSocketConnect(Session sess) {
        System.out.println("Socket Connected: " + sess);
    }

    @OnMessage
    public void onWebSocketText(String message) {
        System.out.println("Received TEXT message: " + message);
    }

    @OnClose
    public void onWebSocketClose(CloseReason reason) {
        System.out.println("Socket Closed: " + reason);
    }

    @OnError
    public void onWebSocketError(Throwable cause) {
        cause.printStackTrace(System.err);
    }
}
 */