package com.xoom.oss.feathercon;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.hamcrest.core.IsEqual;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class WebappServerBuilderTest {

    private String restPathSpec = "/api/*";
    private int serverPort = 8888;
    private String jerseyScanPackages = "com.xoom.oss.feathercon";
    private WebappServerBuilder webappServerBuilder;
    private String resourceBase;
    private FeatherCon server;

    @Before
    public void setup() throws Exception {
        String resourcePath = getClass().getResource("/content-root/anchor").getFile();
        resourceBase = new File(resourcePath).getParentFile().getAbsolutePath() + "/static/";
        webappServerBuilder = new WebappServerBuilder(jerseyScanPackages, restPathSpec, resourceBase, serverPort);
        server = webappServerBuilder.build();
        server.start();
    }

    @After
    public void teardown() throws Exception {
        server.stop();
    }

    @Test
    public void testWebAppBuilder() throws Exception {
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
        assertThat(server.port, equalTo(serverPort));
        assertThat(server.contextName, equalTo("/"));
        server.toString();
    }

    @Test
    public void testStaticContentServer() throws Exception {
        ClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        Client client = Client.create(clientConfig);
        WebResource resource = client.resource(String.format("http://localhost:%d/hello.html", serverPort));
        ClientResponse clientResponse = resource.get(ClientResponse.class);
        String clientHtml = clientResponse.getEntity(String.class);

        FileReader fileReader = new FileReader(String.format("%s/hello.html", resourceBase));
        BufferedReader br = new BufferedReader(fileReader);
        String s;
        StringBuilder persistentHtml = new StringBuilder();
        while ((s = br.readLine()) != null) {
            persistentHtml.append(s).append('\n');
        }

        assertThat(clientHtml, IsEqual.equalTo(persistentHtml.toString()));
    }
}
