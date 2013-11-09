package com.xoom.oss.feathercon;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class SSLConfigurationTest {

    private SSLConfiguration.Builder sslBuilder;

    @Before
    public void setUp() throws Exception {
        sslBuilder = new SSLConfiguration.Builder();
    }

    @Test
    public void testWithKeyStoreFile() throws Exception {
        File keyStoreFile = new File("src/test/resources/keystore.jks");
        sslBuilder.withKeyStoreFile(keyStoreFile);
        assertThat(sslBuilder.keyStoreFile, equalTo(keyStoreFile));
    }

    @Test
    public void testWithKeyStorePassword() throws Exception {
        sslBuilder.withKeyStorePassword("changeit");
        assertThat(sslBuilder.keyStorePassword, equalTo("changeit"));
    }

    @Test
    public void testWithSslPort() throws Exception {
        sslBuilder.withSslPort(8443);
        assertThat(sslBuilder.sslPort, equalTo(8443));
    }

    @Test
    public void testWithSslOnly() throws Exception {
        sslBuilder.withSslOnly(true);
        assertThat(sslBuilder.sslOnly, equalTo(true));
    }
}
