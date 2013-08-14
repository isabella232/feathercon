package com.xoom.oss.feathercon;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import java.net.InetSocketAddress;
import java.util.EnumSet;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FeatherCon {
    public static final Integer DEFAULT_PORT = 8080;
    public final Integer port;
    public final String contextName;
    public final Map<String, Object> servletContextAttributes;
    private final Server server;

    private FeatherCon(Server server, Integer port, String contextName, Map<String, Object> servletContextAttributes) {
        this.server = server;
        this.port = port;
        this.contextName = contextName;
        this.servletContextAttributes = servletContextAttributes;
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
        private Integer port = DEFAULT_PORT;
        private String contextName = "/";

        private Map<String, Object> servletContextAttributes = new HashMap<String, Object>();
        private List<EventListener> servletContextListeners = new LinkedList<EventListener>();
        private List<FilterWrapper> filters = new LinkedList<FilterWrapper>();
        private List<ServletConfiguration> servletConfigurations = new LinkedList<ServletConfiguration>();

        private Boolean built = false;

        public FeatherConBuilder withPort(Integer port) {
            this.port = port;
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

        public FeatherConBuilder withFilter(Class<? extends Filter> filterClass, String pathSpec, EnumSet<DispatcherType> dispatches) {
            filters.add(new FilterWrapper(filterClass, pathSpec, dispatches));
            return this;
        }

        public FeatherConBuilder withServletConfiguration(ServletConfiguration servletConfiguration) {
            servletConfigurations.add(servletConfiguration);
            return this;
        }

        public FeatherConBuilder withContextName(String contextName) {
            this.contextName = contextName;
            return this;
        }

        public FeatherCon build() {
            if (built) {
                throw new IllegalStateException("This builder can be used to produce one server instance.  Please create a new builder.");
            }

            port = port == null ? DEFAULT_PORT : port;
            Server server = new Server(new InetSocketAddress("0.0.0.0", port));

            ServletContextHandler contextHandler = new ServletContextHandler(server, contextName);
            Set<String> pathSpecFilter = new HashSet<String>();
            for (ServletConfiguration servletConfiguration : servletConfigurations) {
                String p = servletConfiguration.pathSpec;
                boolean wasNotAlreadyPresent = pathSpecFilter.add(p);
                if (!wasNotAlreadyPresent) {
                    throw new IllegalStateException(String.format("Another ServletConfiguration is already using this pathSpec %s", p));
                }
                contextHandler.addServlet(servletConfiguration.servletHolder, p);
            }
            HandlerList handlers = new HandlerList();
            handlers.setHandlers(new Handler[]{contextHandler});
            server.setHandler(handlers);

            for (String key : servletContextAttributes.keySet()) {
                contextHandler.getServletContext().setAttribute(key, servletContextAttributes.get(key));
            }
            for (EventListener eventListener : servletContextListeners) {
                contextHandler.addEventListener(eventListener);
            }
            for (FilterWrapper filterBuffer : filters) {
                contextHandler.addFilter(filterBuffer.filterClass, filterBuffer.pathSpec, filterBuffer.dispatches);
            }

            FeatherCon featherCon = new FeatherCon(server, port, contextName, servletContextAttributes);
            built = true;
            logger.info("Built {}", this);
            return featherCon;
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

        @Override
        public String toString() {
            return "FeatherConBuilder{" +
                    "port=" + port +
                    ", contextName='" + contextName + '\'' +
                    ", servletContextAttributes=" + servletContextAttributes +
                    ", servletContextListeners=" + servletContextListeners +
                    ", filters=" + filters +
                    ", servletConfigurations=" + servletConfigurations +
                    ", built=" + built +
                    '}';
        }
    }

}
