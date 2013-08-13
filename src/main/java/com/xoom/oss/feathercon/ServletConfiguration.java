package com.xoom.oss.feathercon;

import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletHolder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ServletConfiguration {
    public final ServletHolder servletHolder;
    public final Class<? extends Servlet> servletClass;
    public final String pathSpec;
    public final String servletName;
    public final Integer initOrder;
    public Map<String, String> initParameters = new HashMap<String, String>();

    private ServletConfiguration(ServletHolder servletHolder, Class<? extends Servlet> servletClass, String pathSpec, String servletName, Integer initOrder, Map<String, String> initParameters) {
        this.servletHolder = servletHolder;
        this.servletClass = servletClass;
        this.pathSpec = pathSpec;
        this.servletName = servletName;
        this.initOrder = initOrder;
        this.initParameters = Collections.unmodifiableMap(initParameters);
    }

    public static class ServletConfigurationBuilder {
        private final Logger logger = LoggerFactory.getLogger(ServletConfigurationBuilder.class);
        private Class<? extends Servlet> servletClass;
        private Integer initOrder = 1;
        private String servletName;
        private String pathSpec = "/*";
        private Map<String, String> initParameters = new HashMap<String, String>();
        private Boolean built = false;

        public ServletConfigurationBuilder withServletClass(@NotNull Class<? extends Servlet> servletClass) {
            this.servletClass = servletClass;
            return this;
        }

        public ServletConfigurationBuilder withServletClassName(@NotNull String servletClassName) {
            try {
                this.servletClass = (Class<? extends Servlet>) getClass().getClassLoader().loadClass(servletClassName);
                return this;
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        public ServletConfigurationBuilder withInitOrder(@NotNull Integer initOrder) {
            this.initOrder = initOrder;
            return this;
        }

        public ServletConfigurationBuilder withServletName(String servletName) {
            this.servletName = servletName;
            return this;
        }

        public ServletConfigurationBuilder withPathSpec(@NotNull String pathSpec) {
            this.pathSpec = pathSpec;
            return this;
        }

        public ServletConfigurationBuilder withInitParameter(String key, String value) {
            initParameters.put(key, value);
            return this;
        }

        public ServletConfigurationBuilder withInitParameters(Map<String, String> initParameters) {
            this.initParameters = initParameters;
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

            ServletConfiguration servletConfiguration = new ServletConfiguration(servletHolder, servletClass, pathSpec, servletName, initOrder, initParameters);
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
                    ", pathSpec='" + pathSpec + '\'' +
                    ", initParameters=" + initParameters +
                    ", built=" + built +
                    '}';
        }
    }

}
