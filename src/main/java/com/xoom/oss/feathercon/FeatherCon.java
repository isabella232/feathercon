package com.xoom.oss.feathercon;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.EventListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FeatherCon {
    public static final Integer DEFAULT_PORT = 8080;
    public Integer port;
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
        if (port == 0) {
            port = server.getConnectors()[0].getLocalPort();
        }
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

    public static class Builder {
        private final Logger logger = LoggerFactory.getLogger(Builder.class);
        protected Integer port = DEFAULT_PORT;
        protected String contextName = "/";
        protected Map<String, Object> servletContextAttributes = new HashMap<String, Object>();
        protected List<EventListener> servletContextListeners = new LinkedList<EventListener>();
        protected List<FilterWrapper> filters = new LinkedList<FilterWrapper>();
        protected List<ServletConfiguration> servletConfigurations = new LinkedList<ServletConfiguration>();
        protected Map<String, String> initParameters = new HashMap<String, String>();
        protected File keyStoreFile;
        protected String keyStorePassword;
        protected Integer sslPort;
        protected Boolean sslOnly;

        private Boolean built = false;

        protected Builder bindAll(Builder builder) {
            this.port = builder.port;
            this.contextName = builder.contextName;
            this.servletContextAttributes = builder.servletContextAttributes;
            this.servletContextListeners = builder.servletContextListeners;
            this.filters = builder.filters;
            this.servletConfigurations = builder.servletConfigurations;
            this.initParameters.putAll(builder.initParameters);
            return this;
        }

        public Builder withPort(Integer port) {
            this.port = port;
            return this;
        }

        public Builder withServletContextAttribute(String key, Object value) {
            servletContextAttributes.put(key, value);
            return this;
        }

        public Builder withServletContextListener(EventListener servletContextListener) {
            servletContextListeners.add(servletContextListener);
            return this;
        }

        public Builder withInitParameter(String key, String value) {
            initParameters.put(key, value);
            return this;
        }

        public Builder withServletContextListener(String servletContextListenerClassName) {
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

        public Builder withFilter(FilterWrapper filterWrapper) {
            filters.add(filterWrapper);
            return this;
        }

        public Builder withServletConfiguration(ServletConfiguration servletConfiguration) {
            servletConfigurations.add(servletConfiguration);
            return this;
        }

        public Builder withContextName(String contextName) {
            this.contextName = contextName;
            return this;
        }

        public Builder withKeyStoreFile(File keyStorePath) {
            this.keyStoreFile = keyStorePath;
            return this;
        }

        public Builder withKeyStorePassword(String keyStorePassword) {
            this.keyStorePassword = keyStorePassword;
            return this;
        }

        public Builder withSslPort(Integer sslPort) {
            this.sslPort = sslPort;
            return this;
        }

        public Builder withSslOnly(Boolean sslOnly) {
            this.sslOnly = sslOnly;
            return this;
        }

        public FeatherCon build() {
            if (built) {
                throw new IllegalStateException("This builder can be used to produce one server instance.  Please create a new builder.");
            }

            port = port == null ? DEFAULT_PORT : port;
            Server server = new Server(new InetSocketAddress("0.0.0.0", port));
            server.setGracefulShutdown(100);

            ServletContextHandler contextHandler = new ServletContextHandler(server, contextName);
            for (ServletConfiguration servletConfiguration : servletConfigurations) {
                for (String pathSpec : servletConfiguration.pathSpecs) {
                    contextHandler.addServlet(servletConfiguration.servletHolder, pathSpec);
                }
            }
            HandlerList handlers = new HandlerList();
            handlers.setHandlers(new Handler[]{contextHandler});
            server.setHandler(handlers);

            for (String key : initParameters.keySet()) {
                contextHandler.setInitParameter(key, initParameters.get(key));
            }
            for (String key : servletContextAttributes.keySet()) {
                contextHandler.getServletContext().setAttribute(key, servletContextAttributes.get(key));
            }
            for (EventListener eventListener : servletContextListeners) {
                contextHandler.addEventListener(eventListener);
            }
            for (FilterWrapper filterBuffer : filters) {
                for (String pathSpec : filterBuffer.pathSpec) {
                    contextHandler.addFilter(filterBuffer.filterHolder, pathSpec, filterBuffer.dispatches);
                }
            }

            if (keyStoreFile != null) {
                if (keyStorePassword == null) {
                    throw new IllegalArgumentException("keystore password has not been specified.");
                }
                if (sslPort == null) {
                    throw new IllegalArgumentException("SSL port has not been specified.");
                }
                if (sslOnly) {
                    for (Connector connector : server.getConnectors()) {
                        server.removeConnector(connector);
                    }
                }
                SslContextFactory sslContextFactory = new SslContextFactory(keyStoreFile.getAbsolutePath());
                sslContextFactory.setKeyStorePassword(keyStorePassword);
                SslSelectChannelConnector connector = new SslSelectChannelConnector(sslContextFactory);
                connector.setPort(sslPort);
                server.addConnector(connector);
            }

            FeatherCon featherCon = new FeatherCon(server, port, contextName, servletContextAttributes);
            built = true;
            logger.info("Built {}", this);
            return featherCon;
        }

        @Override
        public String toString() {
            return "FeatherConBuilder{" +
                    "port=" + port +
                    ", contextName='" + contextName + '\'' +
                    ", servletContextAttributes=" + servletContextAttributes +
                    ", servletContextListeners=" + servletContextListeners +
                    ", filters=" + filters +
                    ", servletConfigurations=" + servletConfigurations + '}';
        }
    }

    @Override
    public String toString() {
        return "FeatherCon{" +
                "port=" + port +
                ", contextName='" + contextName + '\'' +
                ", servletContextAttributes=" + servletContextAttributes +
                ", server=" + server +
                '}';
    }

}
