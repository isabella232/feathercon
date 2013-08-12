package com.xoom.feathercon;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterMapping;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertEquals;
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
        String scanPackages = "com.xoom.feathercon";
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
        FeatherCon featherCon = builder.withServletClassName("org.eclipse.jetty.servlet.DefaultServlet")
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
        FeatherCon featherCon = builder.withServletClassName(DefaultServlet.class).build();
        assertThat(featherCon, is(notNullValue()));
        assertThat(featherCon.port, equalTo(FeatherCon.FeatherConBuilder.DEFAULT_PORT));
        featherCon.start();
        assertThat(featherCon.isRunning(), equalTo(true));
        assertServerUp(FeatherCon.FeatherConBuilder.DEFAULT_PORT);
        featherCon.stop();
    }

    @Test
    public void testNoServletClassSpecified() throws Exception {
        FeatherCon featherCon = new FeatherCon.FeatherConBuilder().build();
        assertThat(featherCon, is(notNullValue()));
        assertThat(featherCon.port, equalTo(FeatherCon.FeatherConBuilder.DEFAULT_PORT));
        featherCon.start();
        assertThat(featherCon.isRunning(), equalTo(true));
        assertServerUp(FeatherCon.FeatherConBuilder.DEFAULT_PORT);
        featherCon.stop();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPicoNotAServletClass() throws Exception {
        FeatherCon featherCon = new FeatherCon.FeatherConBuilder()
                .withServletClassName("java.lang.Object").build();
    }

    @Test
    public void testBuilderFields() throws NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        Object v1 = new Object();
        Object v2 = new Object();
        Integer initOrder = 9;
        Integer port = 90;
        String filterPath = "/foo";
        EnumSet<DispatcherType> dispatcherTypes = EnumSet.of(DispatcherType.ERROR);
        Map<String, String> referenceInitParameters = new HashMap<String, String>();
        referenceInitParameters.put("k1", "v1");
        referenceInitParameters.put("k2", "v2");
        Map<String, Object> referenceServletContextAttributes = new HashMap<String, Object>();
        referenceServletContextAttributes.put("k1", v1);
        referenceServletContextAttributes.put("k2", v2);
        builder.withInitOrder(initOrder)
                .withPort(port)
                .withName("foo")
                .withServletClassName("org.eclipse.jetty.servlet.DefaultServlet")
                .withServletContextListener(servletContextListener)
                .withFilter(AppFilter.class, filterPath, dispatcherTypes);
        for (String key : referenceInitParameters.keySet()) {
            builder.withInitParam(key, referenceInitParameters.get(key));
        }
        for (String key : referenceServletContextAttributes.keySet()) {
            builder.withServletContextAttribute(key, referenceServletContextAttributes.get(key));
        }

        assertThat(initOrder, equalTo(getField("initOrder", builder, FeatherCon.FeatherConBuilder.class, Integer.class)));
        assertThat(port, equalTo(getField("port", builder, FeatherCon.FeatherConBuilder.class, Integer.class)));
        assertThat("foo", equalTo(getField("servletName", builder, FeatherCon.FeatherConBuilder.class, String.class)));
        assertThat(org.eclipse.jetty.servlet.DefaultServlet.class, equalTo(getField("servletClass", builder, FeatherCon.FeatherConBuilder.class, Class.class)));

        Map<String, String> initParameters = getField("initParameters", builder, FeatherCon.FeatherConBuilder.class, Map.class);
        assertThat(2, equalTo(initParameters.size()));
        assertThat(initParameters.get("k1"), equalTo("v1"));
        assertThat(initParameters.get("k2"), equalTo("v2"));


        Map<String, Object> servletContextAttributes = getField("servletContextAttributes", builder, FeatherCon.FeatherConBuilder.class, Map.class);
        assertThat(2, equalTo(servletContextAttributes.size()));
        assertThat(servletContextAttributes.get("k1"), equalTo(v1));
        assertThat(servletContextAttributes.get("k2"), equalTo(v2));


        List<EventListener> servletContextListeners = getField("servletContextListeners", builder, FeatherCon.FeatherConBuilder.class, List.class);
        assertThat(1, equalTo(servletContextListeners.size()));
        assertThat(servletContextListener, equalTo(servletContextListeners.get(0)));


        List<FeatherCon.FeatherConBuilder.FilterWrapper> filters = getField("filters", builder, FeatherCon.FeatherConBuilder.class, List.class);
        assertThat(1, equalTo(filters.size()));
        FeatherCon.FeatherConBuilder.FilterWrapper filterBuffer = filters.get(0);
        assertEquals(AppFilter.class, (filterBuffer.filterClazz));
        assertThat("/foo", equalTo(filterBuffer.pathSpec));
        assertThat(dispatcherTypes, equalTo(filterBuffer.dispatches));

        FeatherCon container = builder.build();

        assertThat(getField("initOrder", builder, FeatherCon.FeatherConBuilder.class, Integer.class), is(nullValue()));
        assertThat(FeatherCon.FeatherConBuilder.DEFAULT_PORT, equalTo(getField("port", builder, FeatherCon.FeatherConBuilder.class, Integer.class)));
        assertThat((getField("servletName", builder, FeatherCon.FeatherConBuilder.class, String.class)), is(nullValue()));
        assertThat(getField("servletClass", builder, FeatherCon.FeatherConBuilder.class, Class.class), is(nullValue()));
        initParameters = getField("initParameters", builder, FeatherCon.FeatherConBuilder.class, Map.class);
        assertThat(0, equalTo(initParameters.size()));
        servletContextAttributes = getField("servletContextAttributes", builder, FeatherCon.FeatherConBuilder.class, Map.class);
        assertThat(0, equalTo(servletContextAttributes.size()));
        servletContextListeners = getField("servletContextListeners", builder, FeatherCon.FeatherConBuilder.class, List.class);
        assertThat(0, equalTo(servletContextListeners.size()));
        filters = getField("filters", builder, FeatherCon.FeatherConBuilder.class, List.class);
        assertThat(0, equalTo(filters.size()));

        ServletHolder servletHolder = getField("servletHolder", container, FeatherCon.class, ServletHolder.class);
        servletHolder.getInitOrder();
        assertThat(initOrder, equalTo(servletHolder.getInitOrder()));

        Map<String, String> initParameters1 = servletHolder.getInitParameters();
        assertThat(referenceInitParameters, equalTo(initParameters1));

        ServletContextHandler contextHandler = getField("container", container, FeatherCon.class, ServletContextHandler.class);
        ContextHandler.Context servletContext = contextHandler.getServletContext();
        Enumeration<String> attributeNames = servletContext.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String key = attributeNames.nextElement();
            assertThat(referenceServletContextAttributes.containsKey(key), is(true));
            assertThat(referenceServletContextAttributes.get(key), equalTo(servletContext.getAttribute(key)));
        }
        EventListener[] eventListeners = contextHandler.getEventListeners();
        assertThat(1, equalTo(eventListeners.length));

        Server server = getField("server", container, FeatherCon.class, Server.class);
        Connector[] connectors = server.getConnectors();
        assertThat(1, equalTo(connectors.length));
        assertThat(port, equalTo(connectors[0].getPort()));

        FilterMapping[] filterMappings = contextHandler.getServletHandler().getFilterMappings();
        assertThat(1, equalTo(filterMappings.length));
        assertThat(filterPath, equalTo(filterMappings[0].getPathSpecs()[0]));
    }

    private <T> T getField(String fieldName, Object instance, Class instanceClass, Class<T> returnType) throws NoSuchFieldException, IllegalAccessException {
        Field declaredField = instanceClass.getDeclaredField(fieldName);
        declaredField.setAccessible(true);
        return returnType.cast(declaredField.get(instance));
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
