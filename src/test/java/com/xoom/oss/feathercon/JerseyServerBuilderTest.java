package com.xoom.oss.feathercon;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MediaType;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class JerseyServerBuilderTest extends BaseTest {

    private FeatherCon server;

    @Before
    public void setUp() throws Exception {
        JerseyServerBuilder jerseyServerBuilder = new JerseyServerBuilder("com.xoom.oss.feathercon", "/api/*");
        server = jerseyServerBuilder.build();
        server.start();
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
    }

    @Test
    public void testJerseyBuilder() throws Exception {
        ClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        Client client = Client.create(clientConfig);
        WebResource resource = client.resource("http://localhost:8080/api/users");
        User user = resource.accept(MediaType.APPLICATION_JSON_TYPE).get(User.class);
        assertThat(user.name, equalTo("Bob Loblaw"));
        assertThat(user.emailAddress, equalTo("bob@lawbomb.example.com"));
    }
}
