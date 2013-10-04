package com.xoom.oss.feathercon;

import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletHolder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServletConfiguration {
    private final Logger logger = LoggerFactory.getLogger(ServletConfiguration.class);
    public final ServletHolder servletHolder;
    public final Class<? extends Servlet> servletClass;
    public final String servletName;
    public final Integer initOrder;
    public final List<String> pathSpecs;
    public final Map<String, String> initParameters;

    private ServletConfiguration(ServletHolder servletHolder, Class<? extends Servlet> servletClass, List<String> pathSpecs, String servletName, Integer initOrder, Map<String, String> initParameters) {
        this.servletHolder = servletHolder;
        this.servletClass = servletClass;
        this.servletName = servletName;
        this.initOrder = initOrder;
        this.initParameters = Collections.unmodifiableMap(initParameters);
        this.pathSpecs = Collections.unmodifiableList(pathSpecs);
        if (pathSpecs.isEmpty()) {
            logger.warn("Servlet {}:{} has no path specs, therefore no query will route here.", servletClass, servletName == null ? "<no servlet name>" : servletName);
        }
    }

    public static class Builder {
        private final Logger logger = LoggerFactory.getLogger(Builder.class);
        Class<? extends Servlet> servletClass;
        Servlet servlet;
        Integer initOrder = 1;
        String servletName;
        List<String> pathSpecs = new ArrayList<String>();
        Map<String, String> initParameters = new HashMap<String, String>();
        Boolean built = false;

        public Builder withServletClass(@NotNull Class<? extends Servlet> servletClass) {
            if (servlet != null) {
                throw new IllegalStateException(String.format("This builder already has been configured with Servlet %s", servlet));
            }
            if (!Servlet.class.isAssignableFrom(servletClass)) {
                throw new IllegalArgumentException(String.format("Provided class %s is not a servlet", servletClass));
            }
            this.servletClass = servletClass;
            return this;
        }

        @SuppressWarnings("unchecked")
        public Builder withServletClassName(@NotNull String servletClassName) {
            if (servlet != null) {
                throw new IllegalStateException(String.format("This builder already has been configured with Servlet %s", servlet));
            }
            try {
                Class<?> aClass = getClass().getClassLoader().loadClass(servletClassName);
                if (!Servlet.class.isAssignableFrom(aClass)) {
                    throw new IllegalArgumentException(String.format("Provided class %s is not a servlet", servletClassName));
                }
                this.servletClass = (Class<? extends Servlet>) aClass;
                return this;
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException(e);
            }
        }

        public Builder withInitOrder(@NotNull Integer initOrder) {
            this.initOrder = initOrder;
            return this;
        }

        public Builder withServletName(String servletName) {
            this.servletName = servletName;
            return this;
        }

        public Builder withServlet(Servlet servlet) {
            if (servletClass != null) {
                throw new IllegalStateException(String.format("This builder already has been configured with Servlet class %s", servletClass));
            }
            this.servlet = servlet;
            return this;
        }

        public Builder withPathSpec(@NotNull String pathSpec) {
            pathSpecs.add(pathSpec);
            return this;
        }

        public Builder withInitParameter(String key, String value) {
            initParameters.put(key, value);
            return this;
        }

        public Builder withInitParameters(Map<String, String> initParameters) {
            this.initParameters.putAll(initParameters);
            return this;
        }

        public ServletConfiguration build() {
            if (built) {
                throw new IllegalStateException("This builder has already built a ServletConfiguration.  Please create a new builder and start over");
            }
            if (servletClass == null) {
                servletClass = DefaultServlet.class;
            }

            ServletHolder servletHolder = servlet != null ? new ServletHolder(servlet) : new ServletHolder(servletClass);

            if (initOrder != null) {
                servletHolder.setInitOrder(initOrder);
            }
            if (servletName != null) {
                servletHolder.setName(servletName);
            }
            servletHolder.setInitParameters(initParameters);

            ServletConfiguration servletConfiguration = new ServletConfiguration(servletHolder, servletClass, pathSpecs, servletName, initOrder, initParameters);
            built = true;
            logger.info("Built {}", this);
            return servletConfiguration;
        }

        @Override
        public String toString() {
            return "ServletConfigurationBuilder{" +
                    "servletClass=" + servletClass +
                    ", initOrder=" + initOrder +
                    ", servletName='" + servletName + '\'' +
                    ", pathSpecs='" + pathSpecs + '\'' +
                    ", initParameters=" + initParameters +
                    ", built=" + built +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "ServletConfiguration{" +
                "servletHolder=" + servletHolder +
                ", servletClass=" + servletClass +
                ", pathSpecs='" + pathSpecs + '\'' +
                ", servletName='" + servletName + '\'' +
                ", initOrder=" + initOrder +
                ", initParameters=" + initParameters +
                '}';
    }
}
