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
public class JerseyServerBuilder extends FeatherCon.Builder {
    /**
     * @param scanPackages Semicolon-separated list of packages to scan for JAX-RS resources.
     */
    public JerseyServerBuilder(String scanPackages) {
        this(scanPackages, "/*");
    }

    /**
     * @param scanPackages Semicolon-separated list of packages to scan for JAX-RS resources.
     * @param pathSpec     Path prefix within the servlet context (e.g., "/api/*"
     */
    public JerseyServerBuilder(String scanPackages, String pathSpec) {
        ServletConfiguration.Builder builder = new ServletConfiguration.Builder();
        builder.withServletClassName("com.sun.jersey.spi.container.servlet.ServletContainer")
                .withServletName("REST Server")
                .withPathSpec(pathSpec)
                .withInitOrder(1)
                .withInitParameter("com.sun.jersey.config.property.packages", scanPackages)
                .withInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");
        ServletConfiguration build = builder.build();
        withServletConfiguration(build);
    }
}