package com.xoom.oss.feathercon;

import org.eclipse.jetty.servlet.DefaultServlet;
import org.junit.After;
import org.junit.Before;

import java.io.File;

public class IntegrationBase extends BaseTest {

    protected int serverPort = 8888;
    protected ServletConfiguration.Builder servletBuilder;
    protected String resourceBase;
    protected FeatherCon.Builder serverBuilder;
    protected FeatherCon server;

    @Before
    public void setup() throws Exception {
        String resourcePath = getClass().getResource("/content-root/anchor").getFile();
        resourceBase = new File(resourcePath).getParentFile().getAbsolutePath() + "/static/";
        servletBuilder = new ServletConfiguration.Builder();
        servletBuilder.withServletClass(DefaultServlet.class).withPathSpec("/*").withInitParameter("resourceBase", resourceBase);
        serverBuilder = new FeatherCon.Builder();
    }

    @After
    public void teardown() throws Exception {
    }
}
