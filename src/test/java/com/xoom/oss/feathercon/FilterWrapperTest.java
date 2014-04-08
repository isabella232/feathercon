package com.xoom.oss.feathercon;

import org.junit.Before;
import org.junit.Test;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.EnumSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

public class FilterWrapperTest extends BaseTest {

    private FilterWrapper.Builder builder;

    @Before
    public void setUp() throws Exception {
        builder = new FilterWrapper.Builder();
    }

    @Test
    public void testWithFilterClass() throws Exception {
        builder.withFilterClass(FilterA.class);
        assertThat(builder.filterClass.equals(FilterA.class), equalTo(true));
    }

    @Test
    public void testWithPathSpec() throws Exception {
        builder.withPathSpec("/apiv1/*").withPathSpec("/apiv2/*");
        assertThat(builder.pathSpecs, is(notNullValue()));
        assertThat(builder.pathSpecs.size(), equalTo(2));
        assertThat(builder.pathSpecs.contains("/apiv1/*"), equalTo(true));
        assertThat(builder.pathSpecs.contains("/apiv2/*"), equalTo(true));
    }

    @Test
    public void testWithInitParameter() throws Exception {
        builder.withInitParameter("k1", "v1").withInitParameter("k2", "v2");
        assertThat(builder.initParams, is(notNullValue()));
        assertThat(builder.initParams.size(), equalTo(2));
        assertThat(builder.initParams.containsKey("k1"), equalTo(true));
        assertThat(builder.initParams.containsKey("k2"), equalTo(true));
        assertThat(builder.initParams.get("k1"), equalTo("v1"));
        assertThat(builder.initParams.get("k2"), equalTo("v2"));
    }

    @Test
    public void testWithFilterInstance() throws Exception {
        Filter filter = new Filter() {

            @Override
            public void init(FilterConfig filterConfig) throws ServletException {

            }

            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

            }

            @Override
            public void destroy() {

            }
        };
        builder.withFilter(filter);
        assertThat(builder.filter.equals(filter), equalTo(true));
    }

    @Test(expected = IllegalStateException.class)
    public void testWithFilterInstanceAndFilterClass() throws Exception {
        Filter filter = new Filter() {

            @Override
            public void init(FilterConfig filterConfig) throws ServletException {

            }

            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

            }

            @Override
            public void destroy() {

            }
        };
        builder.withFilter(filter).withFilterClass(FilterA.class);
        builder.build();
    }

    @Test
    public void testWithDispatcherTypeSet() throws Exception {
        assertThat(builder.dispatcherTypeSet, is(notNullValue()));
        assertThat(builder.dispatcherTypeSet, equalTo(EnumSet.of(DispatcherType.REQUEST)));

        builder.withDispatcherTypeSet(EnumSet.allOf(DispatcherType.class));
        assertThat(builder.dispatcherTypeSet, equalTo(EnumSet.allOf(DispatcherType.class)));
    }

    @Test
    public void testBuild() throws Exception {
        builder.withFilterClass(FilterA.class);
        builder.withPathSpec("/apiv1/*").withPathSpec("/apiv2/*");
        builder.withDispatcherTypeSet(EnumSet.allOf(DispatcherType.class));
        builder.withInitParameter("k1", "v2");
        FilterWrapper build = builder.build();

        build.toString();
    }

    @Test
    public void testToString() throws Exception {
        builder.toString();
    }

    @Test(expected = IllegalStateException.class)
    public void testBuildTwice() throws Exception {
        builder.withFilterClass(FilterA.class);
        builder.build();
        builder.build();
    }

    @Test(expected = IllegalStateException.class)
    public void testNoFilterClass() throws Exception {
        builder.build();
    }
}
