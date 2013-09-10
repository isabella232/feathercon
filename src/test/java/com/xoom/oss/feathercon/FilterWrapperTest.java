package com.xoom.oss.feathercon;

import org.junit.Before;
import org.junit.Test;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

public class FilterWrapperTest {

    private FilterWrapper.FilterWrapperBuilder builder;

    @Before
    public void setUp() throws Exception {
        builder = new FilterWrapper.FilterWrapperBuilder();
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
    public void testWithDispatcherTypeSet() throws Exception {
        builder.withDispatcherTypeSet(EnumSet.allOf(DispatcherType.class));
        assertThat(builder.dispatcherTypeSet, equalTo(EnumSet.allOf(DispatcherType.class)));
    }

    @Test
    public void testBuild() throws Exception {
        builder.withFilterClass(FilterA.class);
        builder.withPathSpec("/apiv1/*").withPathSpec("/apiv2/*");
        builder.withDispatcherTypeSet(EnumSet.allOf(DispatcherType.class));
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
