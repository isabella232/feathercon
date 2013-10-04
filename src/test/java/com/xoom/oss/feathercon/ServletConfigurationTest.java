package com.xoom.oss.feathercon;

import org.eclipse.jetty.servlet.DefaultServlet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class ServletConfigurationTest extends BaseTest {

    private ServletConfiguration.Builder builder;

    @Before
    public void setUp() throws Exception {
        builder = new ServletConfiguration.Builder();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testWithServletClass() throws Exception {
        builder.withServletClass(DefaultServlet.class);
        assertThat(builder.servletClass.equals(DefaultServlet.class), equalTo(true));
    }

    @Test
    public void testWithServlet() throws Exception {
        Servlet servlet = new HttpServlet() {
        };
        builder.withServlet(servlet);
        assertThat(builder.servlet.equals(servlet), equalTo(true));
    }

    @Test
    public void testWithServletClassName() throws Exception {
        builder.withServletClassName(DefaultServlet.class.getCanonicalName());
        assertThat(builder.servletClass.equals(DefaultServlet.class), equalTo(true));
    }

    @Test
    public void testWithInitOrder() throws Exception {
        builder.withInitOrder(1);
        assertThat(builder.initOrder, equalTo(1));
    }

    @Test
    public void testWithServletName() throws Exception {
        String superServlet = "superServlet";
        builder.withServletName(superServlet);
        assertThat(builder.servletName, equalTo(superServlet));
    }

    @Test
    public void testWithPathSpec() throws Exception {
        assertThat(builder.pathSpecs, is(notNullValue()));
        assertThat(builder.pathSpecs.size(), equalTo(0));

        builder.withPathSpec("/css/*");
        builder.withPathSpec("/js/*");
        assertThat(builder.pathSpecs.size(), equalTo(2));
        assertThat(builder.pathSpecs.contains("/css/*"), equalTo(true));
        assertThat(builder.pathSpecs.contains("/js/*"), equalTo(true));
    }

    @Test
    public void testWithInitParameter() throws Exception {
        assertThat(builder.initParameters, is(notNullValue()));
        assertThat(builder.initParameters.size(), equalTo(0));
        builder.withInitParameter("k1", "v1");
        builder.withInitParameter("k2", "v2");

        assertThat(builder.initParameters.size(), equalTo(2));
        assertThat(builder.initParameters.containsKey("k1"), equalTo(true));
        assertThat(builder.initParameters.containsKey("k2"), equalTo(true));

        assertThat(builder.initParameters.get("k1"), equalTo("v1"));
        assertThat(builder.initParameters.get("k2"), equalTo("v2"));
    }

    @Test
    public void testWithInitParameters() throws Exception {
        assertThat(builder.initParameters, is(notNullValue()));
        assertThat(builder.initParameters.size(), equalTo(0));

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("k1", "v1");
        map.put("k2", "v2");
        builder.withInitParameters(map);

        assertThat(builder.initParameters.size(), equalTo(2));
        assertThat(builder.initParameters.containsKey("k1"), equalTo(true));
        assertThat(builder.initParameters.containsKey("k2"), equalTo(true));

        assertThat(builder.initParameters.get("k1"), equalTo("v1"));
        assertThat(builder.initParameters.get("k2"), equalTo("v2"));
    }

    @Test
    public void testBuild() throws Exception {
        builder.withServletClass(DefaultServlet.class)
                .withServletName("superServlet")
                .withInitOrder(1)
                .withInitParameter("k1", "v1")
                .withPathSpec("/css/*")
                .withPathSpec("/js/*");
        ServletConfiguration build = builder.build();
        assertThat(build.initOrder, equalTo(1));
        assertThat(build.servletName, equalTo("superServlet"));
        assertThat(build.servletClass.equals(DefaultServlet.class), equalTo(true));

        assertThat(build.pathSpecs.size(), equalTo(2));
        assertThat(build.pathSpecs.contains("/css/*"), equalTo(true));
        assertThat(build.pathSpecs.contains("/js/*"), equalTo(true));

        assertThat(build.initParameters.containsKey("k1"), equalTo(true));
        assertThat(build.initParameters.get("k1"), equalTo("v1"));
    }

    @Test
    public void testToString() throws Exception {
        builder.toString();
        builder.build().toString();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithServletClassNameNotFound() throws Exception {
        builder.withServletClassName("acme.NoSuchServletClass");
    }

    @Test(expected = IllegalStateException.class)
    public void testBuildTwice() throws Exception {
        builder.build();
        builder.build();
    }

    @Test(expected = IllegalStateException.class)
    public void testBuildWithServletClassAndServletInstance_a() throws Exception {
        builder.withServlet(new HttpServlet() {
        }).withServletClass(DefaultServlet.class);
    }

    @Test(expected = IllegalStateException.class)
    public void testBuildWithServletClassAndServletInstance_b() throws Exception {
        builder
                .withServletClass(DefaultServlet.class)
                .withServlet(new HttpServlet() {
                })
        ;
    }

    @Test(expected = IllegalStateException.class)
    public void testBuildWithServletClassAndServletInstance_c() throws Exception {
        builder
                .withServletClassName(DefaultServlet.class.getCanonicalName())
                .withServlet(new HttpServlet() {
                })
        ;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotAServletClass_a() throws Exception {
        builder.withServletClassName("com.xoom.oss.feathercon.ServletConfigurationTest.Foo");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotAServletClass_b() throws Exception {
        builder.withServletClassName(Foo.class.getCanonicalName());
    }

    private static class Foo {
    }
}
