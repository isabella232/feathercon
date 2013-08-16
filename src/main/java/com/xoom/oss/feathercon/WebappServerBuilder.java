package com.xoom.oss.feathercon;

import org.eclipse.jetty.servlet.DefaultServlet;
import org.jetbrains.annotations.NotNull;

public class WebappServerBuilder extends FeatherCon.FeatherConBuilder {
    public WebappServerBuilder(@NotNull String jerseyScanPackages, @NotNull String restPathSpec, @NotNull String resourceBase, Integer serverPort) {
        bindAll(new JerseyServerBuilder(jerseyScanPackages, restPathSpec)).withPort(serverPort);
        ServletConfiguration.ServletConfigurationBuilder staticContentBuilder = new ServletConfiguration.ServletConfigurationBuilder();
        staticContentBuilder
                .withServletClass(DefaultServlet.class)
                .withPathSpec("/")
                .withServletName("Static Content Servlet")
                .withInitParameter("resourceBase", resourceBase);
        withServletConfiguration(staticContentBuilder.build());
    }
}
