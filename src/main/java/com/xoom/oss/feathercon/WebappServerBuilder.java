package com.xoom.oss.feathercon;

import org.eclipse.jetty.servlet.DefaultServlet;
import org.jetbrains.annotations.NotNull;

/**
 * A builder that produces a JAX-RS Jersey RESTful server with static content hosting in one container.
 */
public class WebappServerBuilder extends FeatherCon.FeatherConBuilder {
    /** Builder to host JAX-RS webapp with static content in one server.
     * @param jerseyScanPackages Semicolon-separated list of packages to scan for JAX-RS resources.
     * @param restPathSpec       The path to the RESTful endpoints, e.g., http://server:port/[restPathSpec]/[some-resource]
     * @param resourceBase       The path to the static content, such as CSS and Javascript files, reachable at
     *                           http://server:port/[static content]
     * @param serverPort         The port on which this server should lister for connections.
     */
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
