package com.xoom.oss.feathercon;

import org.eclipse.jetty.servlet.DefaultServlet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

public class FeatherConTest extends BaseTest {
    FeatherCon.Builder builder;

    @Before
    public void setUp() throws Exception {
        builder = new FeatherCon.Builder();
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
        EnumSet<DispatcherType> dispatcherTypes = EnumSet.allOf(DispatcherType.class);
        FilterWrapper.Builder filterBuilderA = new FilterWrapper.Builder();

        filterBuilderA
                .withFilterClass(FilterA.class)
                .withPathSpec("/css/*")
                .withPathSpec("/js/*")
                .withDispatcherTypeSet(dispatcherTypes);
        FilterWrapper filterWrapperA = filterBuilderA.build();
        builder.withFilter(filterWrapperA);

        FilterWrapper.Builder filterBuilderB = new FilterWrapper.Builder();
        filterBuilderB
                .withFilterClass(FilterB.class)
                .withPathSpec("/apiv1/*")
                .withPathSpec("/apiv2/*")
                .withDispatcherTypeSet(dispatcherTypes);
        FilterWrapper filterWrapperB = filterBuilderB.build();
        builder.withFilter(filterWrapperB);

        assertThat(builder.filters.contains(filterWrapperA), equalTo(true));
        assertThat(builder.filters.contains(filterWrapperB), equalTo(true));
    }

    @Test
    public void testWithServletConfiguration() throws Exception {
        assertThat(builder.servletConfigurations, is(notNullValue()));
        assertThat(builder.servletConfigurations.size(), equalTo(0));

        ServletConfiguration.Builder servletConfigBuilder = new ServletConfiguration.Builder();
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
        assertThat(this.builder.servletConfigurations, is(notNullValue()));
        assertThat(this.builder.servletConfigurations.size(), equalTo(0));

        ServletConfiguration.Builder servletConfigBuilder = new ServletConfiguration.Builder();
        servletConfigBuilder.withServletClass(DefaultServlet.class)
                .withServletName("superServlet")
                .withInitOrder(1)
                .withInitParameter("k1", "v1")
                .withPathSpec("/css/*")
                .withPathSpec("/js/*");
        ServletConfiguration servletConfiguration = servletConfigBuilder.build();
        this.builder.withServletConfiguration(servletConfiguration);
        assertThat(this.builder.servletConfigurations.size(), equalTo(1));
        assertThat(this.builder.servletConfigurations.contains(servletConfiguration), equalTo(true));

        String contextName = "mywebapp";
        this.builder.withContextName(contextName);

        Object contextObject = new Object();
        String contextAttributeKey = "key";
        this.builder.withServletContextAttribute(contextAttributeKey, contextObject);

        this.builder.withInitParameter("k1", "v1");

        this.builder.withServletContextListener(new ContextListener());
        this.builder.withServletContextListener("com.xoom.oss.feathercon.ContextListener");

        EnumSet<DispatcherType> dispatcherTypes = EnumSet.allOf(DispatcherType.class);
        FilterWrapper.Builder builder = new FilterWrapper.Builder();
        builder.withFilterClass(FilterA.class).withPathSpec("/apiv1/*").withPathSpec("/apiv2/*").withDispatcherTypeSet(dispatcherTypes);
        this.builder.withFilter(builder.build());

        FeatherCon build = this.builder.build();
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
