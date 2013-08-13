package com.xoom.oss.feathercon;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private FeatherCon.FeatherConBuilder builder;

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

    @Before
    public void setUp() throws Exception {
        builder = new FeatherCon.FeatherConBuilder();
    }

    @After
    public void tearDown() throws Exception {
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
        System.out.println(user);

        server.stop();
    }

    @Test
    public void testWithStringServletClassName() throws Exception {
        ServletConfiguration.ServletConfigurationBuilder servletConfigBuilder = new ServletConfiguration.ServletConfigurationBuilder();
        ServletConfiguration servletConfig = servletConfigBuilder.withServletClassName("org.eclipse.jetty.servlet.DefaultServlet").build();
        FeatherCon featherCon = builder.withServletConfiguration(servletConfig)
                .withServletContextListener(servletContextListener)
                .withFilter(AppFilter.class, "/foo*", dispatcherTypes)
                .build();
        assertThat(featherCon, is(notNullValue()));
        assertThat(featherCon.port, equalTo(FeatherCon.FeatherConBuilder.DEFAULT_PORT));
        featherCon.start();
        assertThat(featherCon.isRunning(), equalTo(true));
        assertServerUp(FeatherCon.FeatherConBuilder.DEFAULT_PORT);
        featherCon.stop();
    }

    @Test
    public void testWithServletClass() throws Exception {
        ServletConfiguration.ServletConfigurationBuilder servletConfigBuilder = new ServletConfiguration.ServletConfigurationBuilder();
        ServletConfiguration servletConfig = servletConfigBuilder.withServletClass(DefaultServlet.class).build();
        FeatherCon featherCon = builder.withServletConfiguration(servletConfig)
                .withServletContextListener(servletContextListener)
                .withFilter(AppFilter.class, "/foo*", dispatcherTypes)
                .build();
        assertThat(featherCon, is(notNullValue()));
        assertThat(featherCon.port, equalTo(FeatherCon.FeatherConBuilder.DEFAULT_PORT));
        featherCon.start();
        assertThat(featherCon.isRunning(), equalTo(true));
        assertServerUp(FeatherCon.FeatherConBuilder.DEFAULT_PORT);
        featherCon.stop();
    }

    @Test
    public void testNoServletClassSpecified() throws Exception {
        ServletConfiguration.ServletConfigurationBuilder servletConfigBuilder = new ServletConfiguration.ServletConfigurationBuilder();
        ServletConfiguration servletConfiguration = servletConfigBuilder.build();
        FeatherCon featherCon = new FeatherCon.FeatherConBuilder().withServletConfiguration(servletConfiguration).build();
        assertThat(featherCon, is(notNullValue()));
        assertThat(featherCon.port, equalTo(FeatherCon.FeatherConBuilder.DEFAULT_PORT));
        featherCon.start();
        assertThat(featherCon.isRunning(), equalTo(true));
        assertServerUp(FeatherCon.FeatherConBuilder.DEFAULT_PORT);
        featherCon.stop();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPicoNotAServletClass() throws Exception {
        ServletConfiguration.ServletConfigurationBuilder servletConfigBuilder = new ServletConfiguration.ServletConfigurationBuilder();
        servletConfigBuilder.withServletClassName("java.lang.Object").build();
    }

    @Test(expected = IllegalStateException.class)
    public void testNeedNewServletBuilder() throws Exception {
        ServletConfiguration.ServletConfigurationBuilder servletConfigBuilder = new ServletConfiguration.ServletConfigurationBuilder();
        servletConfigBuilder.build();
        servletConfigBuilder.build();
    }

    @Test(expected = IllegalStateException.class)
    public void testNeedNewServerBuilder() throws Exception {
        FeatherCon.FeatherConBuilder builder = new FeatherCon.FeatherConBuilder();
        builder.build();
        builder.build();
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
