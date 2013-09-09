package com.xoom.oss.feathercon;

import org.eclipse.jetty.servlet.DefaultServlet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.DispatcherType;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

public class FeatherConTest {
    FeatherCon.FeatherConBuilder builder;

    @Before
    public void setUp() throws Exception {
        builder = new FeatherCon.FeatherConBuilder();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testWithPort() throws Exception {
        int port = 8080;
        builder.withPort(port);
        assertThat(builder.port, equalTo(port));
    }

    @Test
    public void testWithServletContextAttribute() throws Exception {
        assertThat(builder.servletContextAttributes, is(notNullValue()));
        Object object = new Object();
        String key = "k1";
        builder.withServletContextAttribute(key, object);
        assertThat(builder.servletContextAttributes.containsKey(key), equalTo(true));
        assertThat(builder.servletContextAttributes.get(key), equalTo(object));
    }

    @Test
    public void testWithServletContextListener() throws Exception {
        assertThat(builder.servletContextListeners, is(notNullValue()));
        assertThat(builder.servletContextListeners.size(), equalTo(0));

        ContextListener servletContextListener = new ContextListener();
        builder.withServletContextListener(servletContextListener);
        assertThat(builder.servletContextListeners.contains(servletContextListener), equalTo(true));
        assertThat(builder.servletContextListeners.size(), equalTo(1));
    }

    @Test
    public void testWithInitParameter() throws Exception {
        String key = "k1";
        String value = "v1";

        assertThat(builder.initParameters, is(notNullValue()));
        builder.withInitParameter(key, value);
        assertThat(builder.initParameters.size(), equalTo(1));
        assertThat(builder.initParameters.containsKey(key), equalTo(true));
        assertThat(builder.initParameters.get(key), equalTo(value));
    }

    @Test
    public void testWithFilter() throws Exception {
        List<String> filterAPaths = new ArrayList<String>();
        filterAPaths.add("/css/*");
        filterAPaths.add("/js/*");

        List<String> filterBPaths = new ArrayList<String>();
        filterAPaths.add("/apiv1/*");
        filterAPaths.add("/apiv2/*");

        EnumSet<DispatcherType> dispatcherTypes = EnumSet.allOf(DispatcherType.class);
        builder.withFilter(FilterA.class, filterAPaths, dispatcherTypes);
        builder.withFilter(FilterB.class, filterBPaths, dispatcherTypes);

        assertThat(builder.filters.contains(new FilterWrapper(FilterA.class, filterAPaths, dispatcherTypes)), equalTo(true));
        assertThat(builder.filters.contains(new FilterWrapper(FilterB.class, filterBPaths, dispatcherTypes)), equalTo(true));
    }

    @Test
    public void testWithServletConfiguration() throws Exception {
        assertThat(builder.servletConfigurations, is(notNullValue()));
        assertThat(builder.servletConfigurations.size(), equalTo(0));

        ServletConfiguration.ServletConfigurationBuilder servletConfigBuilder = new ServletConfiguration.ServletConfigurationBuilder();
        servletConfigBuilder.withServletClass(DefaultServlet.class)
                .withServletName("superServlet")
                .withInitOrder(1)
                .withInitParameter("k1", "v1")
                .withPathSpec("/css/*")
                .withPathSpec("/js/*");
        ServletConfiguration servletConfiguration = servletConfigBuilder.build();
        builder.withServletConfiguration(servletConfiguration);
        assertThat(builder.servletConfigurations.size(), equalTo(1));
        assertThat(builder.servletConfigurations.contains(servletConfiguration), equalTo(true));
    }

    @Test
    public void testWithContextName() throws Exception {
        String contextName = "mywebapp";
        builder.withContextName(contextName);
        assertThat(builder.contextName, equalTo(contextName));
    }

    @Test
    public void testBuild() throws Exception {
        assertThat(builder.servletConfigurations, is(notNullValue()));
        assertThat(builder.servletConfigurations.size(), equalTo(0));

        ServletConfiguration.ServletConfigurationBuilder servletConfigBuilder = new ServletConfiguration.ServletConfigurationBuilder();
        servletConfigBuilder.withServletClass(DefaultServlet.class)
                .withServletName("superServlet")
                .withInitOrder(1)
                .withInitParameter("k1", "v1")
                .withPathSpec("/css/*")
                .withPathSpec("/js/*");
        ServletConfiguration servletConfiguration = servletConfigBuilder.build();
        builder.withServletConfiguration(servletConfiguration);
        assertThat(builder.servletConfigurations.size(), equalTo(1));
        assertThat(builder.servletConfigurations.contains(servletConfiguration), equalTo(true));

        String contextName = "mywebapp";
        builder.withContextName(contextName);

        Object contextObject = new Object();
        String contextAttributeKey = "key";
        builder.withServletContextAttribute(contextAttributeKey, contextObject);

        builder.withInitParameter("k1", "v1");

        builder.withServletContextListener(new ContextListener());
        builder.withServletContextListener("com.xoom.oss.feathercon.ContextListener");

        List<String> filterAPaths = new ArrayList<String>();
        filterAPaths.add("/apiv1/*");
        filterAPaths.add("/apiv2/*");
        EnumSet<DispatcherType> dispatcherTypes = EnumSet.allOf(DispatcherType.class);
        builder.withFilter(FilterA.class, filterAPaths, dispatcherTypes);

        FeatherCon build = builder.build();
        assertThat(build.contextName, equalTo(contextName));
        assertThat(build.port, equalTo(8080));
        assertThat(build.servletContextAttributes, is(notNullValue()));
        assertThat(build.servletContextAttributes.containsKey(contextAttributeKey), equalTo(true));
        assertThat(build.servletContextAttributes.get(contextAttributeKey), equalTo(contextObject));
    }

    @Test
    public void testToString() throws Exception {
        builder.toString();
        builder.build().toString();

    }

    @Test(expected = IllegalStateException.class)
    public void testBuiltTwice() throws Exception {
        builder.build();
        builder.build();
    }
}
