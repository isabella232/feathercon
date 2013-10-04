package com.xoom.oss.feathercon;

import org.junit.Ignore;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RunAServerTest {

    @Test
    @Ignore // this is a scratchpad test
    public void runServerTest() throws Exception {
        ServletConfiguration.Builder sConfig = new ServletConfiguration.Builder();
        sConfig.withServlet(new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                System.out.printf("uri: %s\n", req.getRequestURI());
            }
        }).withPathSpec("/*");

        FeatherCon server = new FeatherCon.Builder().withPort(9999).withServletConfiguration(sConfig.build()).build();

        System.out.println("starting");
        server.start();

        while (true) ;
    }
}
