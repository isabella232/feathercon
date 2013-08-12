package com.xoom.feathercon;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import java.net.InetSocketAddress;
import java.util.EnumSet;
import java.util.EventListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FeatherCon {
    public final Integer port;
    private final Server server;
    private final ServletHolder servletHolder;
    private final ServletContextHandler container;

    private FeatherCon(Server server, Integer port, ServletHolder servletHolder, ServletContextHandler container) {
        this.server = server;
        this.port = port;
        this.servletHolder = servletHolder;
        this.container = container;
    }

    public void start() throws Exception {
        server.start();
    }

    public void stop() throws Exception {
        server.stop();
    }

    public boolean isRunning() {
        return server.isRunning();
    }

    public void join() throws InterruptedException {
        server.join();
    }

    public static class FeatherConBuilder {
        private final Logger logger = LoggerFactory.getLogger(FeatherConBuilder.class);

        public static final Integer DEFAULT_PORT = 8080;
        private Integer port = DEFAULT_PORT;
        private Class<? extends Servlet> servletClass;
        private Integer initOrder;
        private String servletName;
        private Map<String, String> initParameters = new HashMap<String, String>();
        private Map<String, Object> servletContextAttributes = new HashMap<String, Object>();
        private List<EventListener> servletContextListeners = new LinkedList<EventListener>();
        private List<FilterWrapper> filters = new LinkedList<FilterWrapper>();

        public FeatherConBuilder withServletClassName(String servletClass) {
            try {
                this.servletClass = (Class<? extends Servlet>) getClass().getClassLoader().loadClass(servletClass);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(String.format("Cannot load specified servlet class %s", servletClass), e);
            }
            return this;
        }

        public FeatherConBuilder withServletClassName(Class<? extends Servlet> servletClass) {
            this.servletClass = servletClass;
            return this;
        }

        public FeatherConBuilder withPort(Integer port) {
            this.port = port;
            return this;
        }

        public FeatherConBuilder withInitParam(String key, String value) {
            initParameters.put(key, value);
            return this;
        }

        public FeatherConBuilder withServletContextAttribute(String key, Object value) {
            servletContextAttributes.put(key, value);
            return this;
        }

        public FeatherConBuilder withServletContextListener(EventListener servletContextListener) {
            servletContextListeners.add(servletContextListener);
            return this;
        }

        public FeatherConBuilder withServletContextListener(String servletContextListenerClassName) {
            try {
                EventListener aClass = (EventListener) getClass().getClassLoader().loadClass(servletContextListenerClassName).newInstance();
                servletContextListeners.add(aClass);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(String.format("Cannot load servlet class %s", servletContextListenerClassName), e);
            } catch (InstantiationException e) {
                throw new RuntimeException(String.format("Cannot load servlet class %s", servletContextListenerClassName), e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(String.format("Cannot load servlet class %s", servletContextListenerClassName), e);
            }
            return this;
        }

        public FeatherConBuilder withName(String servletName) {
            this.servletName = servletName;
            return this;
        }

        public FeatherConBuilder withInitOrder(Integer initOrder) {
            this.initOrder = initOrder;
            return this;
        }

        public FeatherConBuilder withFilter(Class<? extends Filter> filterClass, String pathSpec, EnumSet<DispatcherType> dispatches) {
            filters.add(new FilterWrapper(filterClass, pathSpec, dispatches));
            return this;
        }

        public FeatherCon build() {
            if (servletClass == null) {
                servletClass = DefaultServlet.class;
                logger.info("No servlet class specified, defaulting to %s {}", DefaultServlet.class.getCanonicalName());
            }
            if (!Servlet.class.isAssignableFrom(servletClass)) {
                throw new IllegalArgumentException(String.format("Provided class %s is not a servlet", servletClass.getCanonicalName()));
            }

            ServletHolder servletHolder = new ServletHolder(servletClass);
            if (initOrder != null) {
                servletHolder.setInitOrder(initOrder);
            }
            if (servletName != null) {
                servletHolder.setName(servletName);
            }

            port = port == null ? DEFAULT_PORT : port;
            Server server = new Server(new InetSocketAddress("0.0.0.0", port));

            ServletContextHandler container = new ServletContextHandler(server, "/");
            container.addServlet(servletHolder, "/*");
            HandlerList handlers = new HandlerList();
            handlers.setHandlers(new Handler[]{container});
            server.setHandler(handlers);

            for (String key : initParameters.keySet()) {
                servletHolder.setInitParameter(key, initParameters.get(key));
            }
            for (String key : servletContextAttributes.keySet()) {
                container.getServletContext().setAttribute(key, servletContextAttributes.get(key));
            }
            for (EventListener eventListener : servletContextListeners) {
                container.addEventListener(eventListener);
            }
            for (FilterWrapper filterBuffer : filters) {
                container.addFilter(filterBuffer.filterClass, filterBuffer.pathSpec, filterBuffer.dispatches);
            }

            FeatherCon featherCon = new FeatherCon(server, port, servletHolder, container);
            reset();
            return featherCon;
        }

        private void reset() {
            servletClass = null;
            port = DEFAULT_PORT;
            initOrder = null;
            servletName = null;
            initParameters.clear();
            servletContextAttributes.clear();
            servletContextListeners.clear();
            filters.clear();
        }

        class FilterWrapper {
            public final Class<? extends Filter> filterClass;
            public final String pathSpec;
            public final EnumSet<DispatcherType> dispatches;

            public FilterWrapper(Class<? extends Filter> filterClass, String pathSpec, EnumSet<DispatcherType> dispatches) {
                this.filterClass = filterClass;
                this.pathSpec = pathSpec;
                this.dispatches = dispatches;
            }
        }
    }

}
