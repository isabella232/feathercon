package com.xoom.oss.feathercon;

import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletHolder;
import org.jetbrains.annotations.NotNull;

import javax.servlet.Servlet;
import java.util.Collections;
import java.util.Map;

public class ServletConfiguration {
    public final String servletName;
    public final String pathSpec;
    public final Integer initOrder;
    public final Map<String, String> initParameters;
    public final ServletHolder servletHolder;

    public ServletConfiguration(ServletHolder servletHolder, String pathSpec, String servletName, Integer initOrder, Map<String, String> initParameters) {
        this.servletHolder = servletHolder;
        this.pathSpec = pathSpec;
        this.servletName = servletName;
        this.initOrder = initOrder;
        this.initParameters = Collections.unmodifiableMap(initParameters);
    }

    public static class ServletConfigurationBuilder {
        private Class<? extends Servlet> servletClass;
        private Integer initOrder = 1;
        private String servletName;
        private String pathSpec;
        private Map<String, String> initParameters;

        public ServletConfigurationBuilder withServletClass(@NotNull Class<? extends Servlet> servletClass) {
            this.servletClass = servletClass;
            return this;
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

        public ServletConfigurationBuilder withInitParameters(Map<String, String> initParameters) {
            this.initParameters = initParameters;
            return this;
        }

        public ServletConfiguration build() {
            if (servletClass == null) {
                servletClass = DefaultServlet.class;
            }
            if (!Servlet.class.isAssignableFrom(servletClass)) {
                throw new IllegalArgumentException(String.format("Provided class %s is not a servlet", servletClass.getCanonicalName()));
            }
            if (pathSpec == null) {
                throw new RuntimeException(String.format("pathSpec for servlet %s is null", servletClass));
            }
            ServletHolder servletHolder = new ServletHolder(servletClass);
            if (initOrder != null) {
                servletHolder.setInitOrder(initOrder);
            }
            if (servletName != null) {
                servletHolder.setName(servletName);
            }
            if (initParameters != null) {
                for (String key : initParameters.keySet()) {
                    servletHolder.setInitParameter(key, initParameters.get(key));
                }
            }
            return new ServletConfiguration(servletHolder, pathSpec, servletName, initOrder, initParameters);
        }
    }

}
