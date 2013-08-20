package com.xoom.oss.feathercon;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.fail;

public class FeatherConTest {

    private ServletContextListener servletContextListener = new ContextListener();

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

    @Test(expected = RuntimeException.class)
    public void testJerseyBuilderListenerNotFound() {
        FeatherCon.FeatherConBuilder rest = new JerseyServerBuilder("com.xoom.scanpkgs")
                .withPort(8888).withContextName("rest").withServletContextAttribute("a", "b")
                .withServletContextListener("com.xoom.oss.feathercon.What")
                .withServletContextListener(servletContextListener);
        FeatherCon build = rest.build();
        assertThat(build.contextName, equalTo("rest"));
        assertThat(build.servletContextAttributes.isEmpty(), equalTo(false));
        assertThat(build.servletContextAttributes.containsKey("a"), equalTo(true));
        assertThat(build.servletContextAttributes.get("a"), equalTo((Object) "b"));
    }

    @Test(expected = RuntimeException.class)
    public void testSpecifiedServletClassListenerIsAbstract() {
        FeatherCon.FeatherConBuilder rest = new JerseyServerBuilder("com.xoom.scanpkgs")
                .withServletContextListener("com.xoom.oss.feathercon.AbstractListener");
    }

    @Test(expected = RuntimeException.class)
    public void testSpecifiedServletClassListenerIsNotAccessible() {
        FeatherCon.FeatherConBuilder rest = new JerseyServerBuilder("com.xoom.scanpkgs")
                .withServletContextListener("com.xoom.oss.feathercon.otherpkg.AccessIssuesListener");
    }

    @Test
    public void testJerseyBuilder() {
        FeatherCon.FeatherConBuilder rest = new JerseyServerBuilder("com.xoom.scanpkgs")
                .withPort(8888).withContextName("rest").withServletContextAttribute("a", "b")
                .withServletContextListener("com.xoom.oss.feathercon.ContextListener");
        rest.toString();
        FeatherCon build = rest.build();
        assertThat(build.port, equalTo(8888));
        assertThat(build.contextName, equalTo("rest"));
        assertThat(build.servletContextAttributes.isEmpty(), equalTo(false));
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

        String resourcePath = getClass().getResource("/content-root/anchor").getFile();
        String resourceBase = new File(resourcePath).getParentFile().getAbsolutePath() + "/static/";
        ServletConfiguration.ServletConfigurationBuilder staticContentBuilder = new ServletConfiguration.ServletConfigurationBuilder();
        staticContentBuilder
                .withServletClass(DefaultServlet.class)
                .withPathSpec("/")
                .withInitParameter("resourceBase", resourceBase);
        staticContentBuilder.toString();
        FeatherCon.FeatherConBuilder serverBuilder = new FeatherCon.FeatherConBuilder();
        serverBuilder.withServletConfiguration(jerseyBuilder.build()).withServletConfiguration(staticContentBuilder.build());

        FeatherCon server = serverBuilder.build();
        server.start();

        // consume resources and compare
        ClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        Client client = Client.create(clientConfig);
        WebResource resource = client.resource(String.format("http://localhost:8080/%s/users", jerseyPathSpec));
        User user = resource.accept(MediaType.APPLICATION_JSON_TYPE).get(User.class);
        assertThat(user.name, equalTo("Bob Loblaw"));
        assertThat(user.emailAddress, equalTo("bob@lawbomb.example.com"));

        resource = client.resource("http://localhost:8080/hello.html");
        ClientResponse clientResponse = resource.get(ClientResponse.class);
        String html = clientResponse.getEntity(String.class);
        FileReader fileReader = new FileReader(String.format("%s/hello.html", resourceBase));
        BufferedReader br = new BufferedReader(fileReader);
        String s;
        StringBuilder sb = new StringBuilder();
        while ((s = br.readLine()) != null) {
            sb.append(s).append('\n');
        }
        assertThat(html, equalTo(sb.toString()));

        server.stop();
    }

    @Test
    public void testWithStringServletClassName() throws Exception {
        Map<String, String> initParams = new HashMap<String, String>();
        initParams.put("key", "value");
        ServletConfiguration servletConfig = servletConfigBuilder.withServletClassName("org.eclipse.jetty.servlet.DefaultServlet")
                .withInitParameters(initParams)
                .build();
        servletConfig.toString();
        assertThat(servletConfig.servletClass.equals(DefaultServlet.class), equalTo(true));
        assertThat(servletConfig.pathSpec, equalTo("/*"));
        assertThat(servletConfig.initOrder, equalTo(1));
        assertThat(servletConfig.servletName, is(nullValue()));
        assertThat(servletConfig.initParameters.isEmpty(), equalTo(false));
        assertThat(servletConfig.initParameters.containsKey("key"), equalTo(true));
        assertThat(servletConfig.initParameters.get("key"), equalTo("value"));

        FeatherCon server = featherConBuilder.withServletConfiguration(servletConfig)
                .withServletContextListener(servletContextListener)
                .withServletContextListener("com.xoom.oss.feathercon.ContextListener")
                .withFilter(AppFilter.class, "/foo*", dispatcherTypes)
                .build();
        assertThat(server, is(notNullValue()));
        assertThat(server.port, equalTo(FeatherCon.DEFAULT_PORT));
        server.start();
        assertThat(server.isRunning(), equalTo(true));
        assertServerUp(FeatherCon.DEFAULT_PORT);
        server.stop();
    }

    @Test
    public void testWithServletClass() throws Exception {
        ServletConfiguration servletConfig = servletConfigBuilder.withServletClass(DefaultServlet.class)
                .withServletName("fooServlet")
                .withInitParameter("key", "value")
                .build();
        assertThat(servletConfig.servletName, equalTo("fooServlet"));
        assertThat(servletConfig.initParameters.isEmpty(), equalTo(false));
        assertThat(servletConfig.initParameters.containsKey("key"), equalTo(true));
        assertThat(servletConfig.initParameters.get("key"), equalTo("value"));

        FeatherCon server = featherConBuilder.withServletConfiguration(servletConfig)
                .withServletContextListener(servletContextListener)
                .withFilter(AppFilter.class, "/foo*", dispatcherTypes)
                .build();
        assertThat(server, is(notNullValue()));
        assertThat(server.port, equalTo(FeatherCon.DEFAULT_PORT));
        server.start();
        assertThat(server.isRunning(), equalTo(true));
        assertServerUp(FeatherCon.DEFAULT_PORT);
        server.stop();
    }

    @Test
    public void testNoServletClassSpecified() throws Exception {
        ServletConfiguration servletConfiguration = servletConfigBuilder.build();
        assertThat(servletConfiguration.servletClass.equals(DefaultServlet.class), equalTo(true));

        FeatherCon server = featherConBuilder.withServletConfiguration(servletConfiguration).build();
        assertThat(server, is(notNullValue()));
        assertThat(server.port, equalTo(FeatherCon.DEFAULT_PORT));
        server.start();
        assertThat(server.isRunning(), equalTo(true));
        assertServerUp(FeatherCon.DEFAULT_PORT);
        server.stop();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotAServletClass() throws Exception {
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

    @Test(expected = IllegalArgumentException.class)
    public void testServletClassNotFound() {
        servletConfigBuilder.withServletClassName("com.example.NoSuchServlet");
    }

    @Test
    public void testNullPort() {
        ServletConfiguration servletConfiguration = servletConfigBuilder.build();
        assertThat(servletConfiguration.servletClass.equals(DefaultServlet.class), equalTo(true));

        FeatherCon server = featherConBuilder.withServletConfiguration(servletConfiguration).withPort(null).build();
        assertThat(server, is(notNullValue()));
        assertThat(server.port, equalTo(FeatherCon.DEFAULT_PORT));
    }

    @Test
    public void testNullInitOrder() {
        ServletConfiguration servletConfiguration = servletConfigBuilder.withInitOrder(null).build();
        assertThat(servletConfiguration.servletClass.equals(DefaultServlet.class), equalTo(true));
        assertThat(servletConfiguration.initOrder, is(nullValue()));
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
