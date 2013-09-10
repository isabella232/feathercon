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
        Integer initOrder = 1;
        String servletName;
        List<String> pathSpecs = new ArrayList<String>();
        Map<String, String> initParameters = new HashMap<String, String>();
        Boolean built = false;

        public Builder withServletClass(@NotNull Class<? extends Servlet> servletClass) {
            this.servletClass = servletClass;
            return this;
        }

        public Builder withServletClassName(@NotNull String servletClassName) {
            try {
                this.servletClass = (Class<? extends Servlet>) getClass().getClassLoader().loadClass(servletClassName);
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServletConfiguration that = (ServletConfiguration) o;

        if (!initOrder.equals(that.initOrder)) return false;
        if (!initParameters.equals(that.initParameters)) return false;
        if (!pathSpecs.equals(that.pathSpecs)) return false;
        if (!servletClass.equals(that.servletClass)) return false;
        if (!servletHolder.equals(that.servletHolder)) return false;
        if (!servletName.equals(that.servletName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = servletHolder.hashCode();
        result = 31 * result + servletClass.hashCode();
        result = 31 * result + servletName.hashCode();
        result = 31 * result + initOrder.hashCode();
        result = 31 * result + pathSpecs.hashCode();
        result = 31 * result + initParameters.hashCode();
        return result;
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
