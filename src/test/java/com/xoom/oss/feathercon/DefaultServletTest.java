package com.xoom.oss.feathercon;

import org.eclipse.jetty.servlet.DefaultServlet;
import org.junit.Before;

public class DefaultServletTest {

    private FeatherCon.FeatherConBuilder featherConBuilder;
    private ServletConfiguration.ServletConfigurationBuilder servletConfigBuilder;

    @Before
    public void beforeMethod() {
        featherConBuilder = new FeatherCon.FeatherConBuilder();
        servletConfigBuilder = new ServletConfiguration.ServletConfigurationBuilder();
    }

    // http://download.eclipse.org/jetty/stable-7/apidocs/org/eclipse/jetty/servlet/DefaultServlet.html
//    @Test
    public void testDefaultServlet() throws Exception {
        System.out.println("starting...");
        servletConfigBuilder
                .withServletClass(DefaultServlet.class)
                .withPathSpec("/")
                .withInitParameter("dirAllowed", "true")
                .withInitParameter("resourceBase", "/tmp/");
        ServletConfiguration build = servletConfigBuilder.build();
        FeatherCon server = featherConBuilder.withServletConfiguration(build).build();
        server.start();
        System.out.println("started.");

        boolean spin = true;
        while (spin) {
        }

    }

//    @Test
    public void testX() throws Exception {
        String scanPackages = "com.xoom.oss.feathercon";
        FeatherCon.FeatherConBuilder jerseyServerBuilder = new JerseyServerBuilder(scanPackages, "/api/*");
        ServletConfiguration.ServletConfigurationBuilder defaultServletBuilder = new ServletConfiguration.ServletConfigurationBuilder();
        defaultServletBuilder
                .withServletClass(DefaultServlet.class)
                .withPathSpec("/")
                .withInitParameter("dirAllowed", "true")
                .withInitParameter("resourceBase", "/tmp/");
        ServletConfiguration defaultServletConfig = defaultServletBuilder.build();

        jerseyServerBuilder.withServletConfiguration(defaultServletConfig);
        FeatherCon server = jerseyServerBuilder.build();

        server.start();

//        ClientConfig clientConfig = new DefaultClientConfig();
//        clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
//        Client client = Client.create(clientConfig);
//        WebResource resource = client.resource("http://localhost:8080/users");
//        User user = resource.accept(MediaType.APPLICATION_JSON_TYPE).get(User.class);
//        logger.info("{}", user);

        boolean spin = true;
        while (spin) {
        }
//        server.stop();

    }

}
