package com.xoom.feathercon;

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
        withServletClassName("com.sun.jersey.spi.container.servlet.ServletContainer")
                .withInitOrder(1)
                .withInitParam("com.sun.jersey.config.property.packages", scanPackages)
                .withInitParam("com.sun.jersey.api.json.POJOMappingFeature", "true");
    }
}