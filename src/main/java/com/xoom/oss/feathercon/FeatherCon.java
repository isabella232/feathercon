package com.xoom.oss.feathercon;

import org.eclipse.jetty.server.AbstractConnectionFactory;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.EventListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

// ssl migration: http://goo.gl/26lM7

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

    /**
     * Return the port of the first connector supported by HttpConnectionFactory.  When using an ephemeral port,
     * this value will be nonzero, unlike the value of this.port.
     *
     * @return http port
     */
    public Integer getHttpPort() {
        return getPort(Scheme.HTTP);
    }

    /**
     * Return the port of the first connector supported by SslConnectionFactory.  When using an ephemeral port,
     * this value will be nonzero, unlike the value of this.port.
     *
     * @return https port
     */
    public Integer getHttpsPort() {
        return getPort(Scheme.HTTPS);
    }

    private Integer getPort(Scheme scheme) {
        for (Connector connector : server.getConnectors()) {
            if (connector instanceof ServerConnector) {
                ServerConnector serverConnector = (ServerConnector) connector;
                Collection<ConnectionFactory> connectionFactories = serverConnector.getConnectionFactories();
                for (ConnectionFactory connectionFactory : connectionFactories) {
                    Class<? extends AbstractConnectionFactory> connectorClass;
                    switch (scheme) {
                        case HTTP:
                            connectorClass = HttpConnectionFactory.class;
                            break;
                        case HTTPS:
                            connectorClass = SslConnectionFactory.class;
                            break;
                        default:
                            throw new UnsupportedOperationException("No such scheme.");
                    }
                    if (connectorClass.isAssignableFrom(connectionFactory.getClass())) {
                        return serverConnector.getLocalPort();
                    }
                }
            }
        }
        return null;
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
        protected SSLConfiguration sslConfiguration;

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

        public Builder withSslConfiguration(SSLConfiguration sslConfiguration) {
            this.sslConfiguration = sslConfiguration;
            return this;
        }

        public FeatherCon build() {
            if (built) {
                throw new IllegalStateException("This builder can be used to produce one server instance.  Please create a new builder.");
            }

            Server server = new Server();

            port = port == null ? DEFAULT_PORT : port;

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

            // Jetty 9 connector config: http://goo.gl/26lM7

            HttpConfiguration http_config = new HttpConfiguration();
            http_config.setOutputBufferSize(32768);

            if (sslConfiguration == null || !sslConfiguration.sslOnly) {
                ServerConnector http = new ServerConnector(server, new HttpConnectionFactory(http_config));
                http.setPort(port);
                http.setIdleTimeout(30000);
                server.addConnector(http);
            }

            if (sslConfiguration != null) {
                http_config.setSecureScheme("https");

                HttpConfiguration https_config = new HttpConfiguration(http_config);
                SecureRequestCustomizer customizer = new SecureRequestCustomizer();
                https_config.addCustomizer(customizer);

                // exclude cipher suites: https://jira.atlassian.com/browse/CRUC-6594
                // http://www.eclipse.org/jetty/documentation/current/configuring-ssl.html
                SslContextFactory sslContextFactory = new SslContextFactory(sslConfiguration.keyStoreFile.getAbsolutePath());
                sslContextFactory.setExcludeCipherSuites(sslConfiguration.excludeCipherSuites.toArray(new String[sslConfiguration.excludeCipherSuites.size()]));
                sslContextFactory.setKeyStorePassword(sslConfiguration.keyStorePassword);

                ServerConnector https = new ServerConnector(server, new SslConnectionFactory(sslContextFactory, "http/1.1"));
                https.setPort(sslConfiguration.sslPort);
                https.setIdleTimeout(500000);
                server.addConnector(https);
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
