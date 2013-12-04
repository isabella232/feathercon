package com.xoom.oss.feathercon;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import org.junit.Test;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.EnumSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class FilterIntegrationTest extends IntegrationBase {
    @Test
    public void testFiltering() throws Exception {
        FilterWrapper.Builder filterBuilder = new FilterWrapper.Builder();
        FilterWrapper filterWrapper = filterBuilder.withFilterClass(FilterT.class)
                .withPathSpec("/*")
                .withDispatcherTypeSet(EnumSet.of(DispatcherType.REQUEST))
                .withInitParameter("k1", "v1").build();
        serverBuilder.withServletConfiguration(servletBuilder.build()).withFilter(filterWrapper);
        server = serverBuilder.withPort(serverPort).build();
        server.start();

        ClientConfig clientConfig = new DefaultClientConfig();
        Client client = Client.create(clientConfig);
        WebResource resource = client.resource(String.format("http://localhost:%d/hello.html", serverPort));
        ClientResponse clientResponse = resource.get(ClientResponse.class);
        assertThat(clientResponse.getHeaders().get("h1").get(0), equalTo("h2"));
        assertThat(clientResponse.getHeaders().get("k1").get(0), equalTo("v1"));

        server.stop();
    }

    public static class FilterT implements Filter {

        private FilterConfig filterConfig;

        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
            this.filterConfig = filterConfig;
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            HttpServletResponse r = (HttpServletResponse) response;
            r.setHeader("h1", "h2");
            r.setHeader("k1", filterConfig.getInitParameter("k1"));
            chain.doFilter(request, response);
        }

        @Override
        public void destroy() {
        }
    }
}
