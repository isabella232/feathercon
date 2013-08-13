package com.xoom.oss.feathercon;

/**
 * Jersey servlet builder.  To use this, these Jersey dependencies need to be on the classpath
 * at runtime:
 * <ul>
 * <li>com.sun.jersey:jersey-core</li>
 * <li>com.sun.jersey:jersey-json</li>
 * <li>com.sun.jersey:jersey-server</li>
 * <li>com.sun.jersey:jersey-servlet</li>
 * </ul>
 * <p/>
 */
public class JerseyServerBuilder extends FeatherCon.FeatherConBuilder {
    /**
     * @param scanPackages Semicolon-separated list of packages to scan for JAX-RS resources.
     */
    public JerseyServerBuilder(String scanPackages) {
        ServletConfiguration.ServletConfigurationBuilder servletConfigurationBuilder = new ServletConfiguration.ServletConfigurationBuilder();
        servletConfigurationBuilder.withServletClassName("com.sun.jersey.spi.container.servlet.ServletContainer")
                .withServletName("REST Server")
                .withInitOrder(1)
                .withInitParameter("com.sun.jersey.config.property.packages", scanPackages)
                .withInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");
        ServletConfiguration build = servletConfigurationBuilder.build();
        withServletConfiguration(build);
    }
}