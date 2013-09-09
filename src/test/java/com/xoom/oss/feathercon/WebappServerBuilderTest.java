package com.xoom.oss.feathercon;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class WebappServerBuilderTest {

    @Test
    public void testWebAppBuilder() throws Exception {
        String resourceBase = "/tmp";
        String restPathSpec = "/api/*";
        int serverPort = 8888;
        String jerseyScanPackages = "com.xoom.oss.feathercon";
        WebappServerBuilder webappServerBuilder = new WebappServerBuilder(jerseyScanPackages, restPathSpec, resourceBase, serverPort);

        assertThat(webappServerBuilder.servletConfigurations.size(), equalTo(2));

        ServletConfiguration jaxRsConfig = webappServerBuilder.servletConfigurations.get(0);
        assertThat(jaxRsConfig.servletClass.equals(ServletContainer.class), equalTo(true));
        assertThat(jaxRsConfig.initParameters.get("com.sun.jersey.config.property.packages"), equalTo(jerseyScanPackages));
        assertThat(jaxRsConfig.initParameters.get("com.sun.jersey.api.json.POJOMappingFeature"), equalTo("true"));
        assertThat(jaxRsConfig.pathSpecs.get(0), equalTo(restPathSpec));

        ServletConfiguration staticConfig = webappServerBuilder.servletConfigurations.get(1);
        assertThat(staticConfig.servletClass.equals(DefaultServlet.class), equalTo(true));
        assertThat(staticConfig.initParameters.get("resourceBase"), equalTo(resourceBase));
        assertThat(staticConfig.pathSpecs.get(0), equalTo("/"));

        FeatherCon server = webappServerBuilder.build();
        assertThat(server.port, equalTo(serverPort));
        assertThat(server.contextName, equalTo("/"));

        server.toString();
    }
}
