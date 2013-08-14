package com.xoom.oss.feathercon;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.Socket;
import java.util.EnumSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.fail;

public class FeatherConTest {

    private final Logger logger = LoggerFactory.getLogger(FeatherConTest.class);

    private ServletContextListener servletContextListener = new ServletContextListener() {
        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        @Override
        public void contextInitialized(ServletContextEvent sce) {
            logger.info("ServletContextListener init");
        }

        @Override
        public void contextDestroyed(ServletContextEvent sce) {
            logger.info("ServletContextListener destroy");
        }
    };

    private EnumSet<DispatcherType> dispatcherTypes = EnumSet.of(DispatcherType.ERROR);
    private FeatherCon.FeatherConBuilder featherConBuilder;
    private ServletConfiguration.ServletConfigurationBuilder servletConfigBuilder;

    @BeforeClass
    public static void setupClass() {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    @Before
    public void beforeMethod() {
        featherConBuilder = new FeatherCon.FeatherConBuilder();
        servletConfigBuilder = new ServletConfiguration.ServletConfigurationBuilder();
    }

    @Test
    public void testJerseyServer() throws Exception {
        String scanPackages = "com.xoom.oss.feathercon";
        FeatherCon server = new JerseyServerBuilder(scanPackages).build();
        server.start();

        ClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        Client client = Client.create(clientConfig);
        WebResource resource = client.resource("http://localhost:8080/users");
        User user = resource.accept(MediaType.APPLICATION_JSON_TYPE).get(User.class);
        logger.info("{}", user);

        server.stop();
    }

    @Test
    public void testJerseyServerWithStaticContent() throws Exception {
        String scanPackages = "com.xoom.oss.feathercon";
        String jerseyPathSpec = "api";
        ServletConfiguration.ServletConfigurationBuilder jerseyBuilder = new ServletConfiguration.ServletConfigurationBuilder();
        jerseyBuilder.withServletClassName("com.sun.jersey.spi.container.servlet.ServletContainer")
                .withServletName("REST Server")
                .withPathSpec(String.format("/%s/*", jerseyPathSpec))
                .withInitOrder(1)
                .withInitParameter("com.sun.jersey.config.property.packages", scanPackages)
                .withInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");

        ServletConfiguration.ServletConfigurationBuilder staticContentBuilder = new ServletConfiguration.ServletConfigurationBuilder();
        staticContentBuilder
                .withServletClass(DefaultServlet.class)
                .withPathSpec("/")
                .withInitParameter("dirAllowed", "true")
                .withInitParameter("resourceBase", "/tmp/");

        FeatherCon.FeatherConBuilder serverBuilder = new FeatherCon.FeatherConBuilder();
        serverBuilder.withServletConfiguration(jerseyBuilder.build()).withServletConfiguration(staticContentBuilder.build());

        FeatherCon server = serverBuilder.build();
        server.start();

        ClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        Client client = Client.create(clientConfig);
        WebResource resource = client.resource(String.format("http://localhost:8080/%s/users", jerseyPathSpec));
        User user = resource.accept(MediaType.APPLICATION_JSON_TYPE).get(User.class);
        logger.info("{}", user);

        server.stop();
    }

    @Test
    public void testWithStringServletClassName() throws Exception {
        ServletConfiguration servletConfig = servletConfigBuilder.withServletClassName("org.eclipse.jetty.servlet.DefaultServlet").build();
        FeatherCon featherCon = featherConBuilder.withServletConfiguration(servletConfig)
                .withServletContextListener(servletContextListener)
                .withFilter(AppFilter.class, "/foo*", dispatcherTypes)
                .build();
        assertThat(featherCon, is(notNullValue()));
        assertThat(featherCon.port, equalTo(FeatherCon.DEFAULT_PORT));
        featherCon.start();
        assertThat(featherCon.isRunning(), equalTo(true));
        assertServerUp(FeatherCon.DEFAULT_PORT);
        featherCon.stop();
    }

    @Test
    public void testWithServletClass() throws Exception {
        ServletConfiguration servletConfig = servletConfigBuilder.withServletClass(DefaultServlet.class).build();
        FeatherCon featherCon = featherConBuilder.withServletConfiguration(servletConfig)
                .withServletContextListener(servletContextListener)
                .withFilter(AppFilter.class, "/foo*", dispatcherTypes)
                .build();
        assertThat(featherCon, is(notNullValue()));
        assertThat(featherCon.port, equalTo(FeatherCon.DEFAULT_PORT));
        featherCon.start();
        assertThat(featherCon.isRunning(), equalTo(true));
        assertServerUp(FeatherCon.DEFAULT_PORT);
        featherCon.stop();
    }

    @Test
    public void testNoServletClassSpecified() throws Exception {
        ServletConfiguration servletConfiguration = servletConfigBuilder.build();
        FeatherCon featherCon = featherConBuilder.withServletConfiguration(servletConfiguration).build();
        assertThat(featherCon, is(notNullValue()));
        assertThat(featherCon.port, equalTo(FeatherCon.DEFAULT_PORT));
        featherCon.start();
        assertThat(featherCon.isRunning(), equalTo(true));
        assertServerUp(FeatherCon.DEFAULT_PORT);
        featherCon.stop();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPicoNotAServletClass() throws Exception {
        servletConfigBuilder.withServletClassName("java.lang.Object").build();
    }

    @Test(expected = IllegalStateException.class)
    public void testNeedNewServletBuilder() throws Exception {
        servletConfigBuilder.build();
        servletConfigBuilder.build();
    }

    @Test(expected = IllegalStateException.class)
    public void testNeedNewServerBuilder() throws Exception {
        featherConBuilder.build();
        featherConBuilder.build();
    }

    @Test(expected = IllegalStateException.class)
    public void testPathSpecCollision() {
        new FeatherCon.FeatherConBuilder()
                .withServletConfiguration(new ServletConfiguration.ServletConfigurationBuilder().withPathSpec("/*").build())
                .withServletConfiguration(new ServletConfiguration.ServletConfigurationBuilder().withPathSpec("/*").build())
                .build();
    }

    private void assertServerUp(int port) {
        try {
            Socket socket = new Socket("localhost", port);
            closeQuietly(socket);
        } catch (Exception e) {
            fail();
        }
    }

    private void closeQuietly(Socket socket) {
        try {
            socket.close();
        } catch (IOException e) {
        }
    }

    public static class AppFilter implements Filter {
        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        }

        @Override
        public void destroy() {
        }
    }


}
