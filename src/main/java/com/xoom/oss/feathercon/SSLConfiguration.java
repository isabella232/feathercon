package com.xoom.oss.feathercon;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SSLConfiguration {
    public final File keyStoreFile;
    public final String keyStorePassword;
    public final Integer sslPort;
    public final boolean sslOnly;
    public final List<String> excludeCipherSuites;

    private SSLConfiguration(File keyStoreFile, String keyStorePassword, Integer sslPort, boolean sslOnly, List<String> excludeCipherSuites) {
        this.keyStoreFile = keyStoreFile;
        this.keyStorePassword = keyStorePassword;
        this.sslPort = sslPort;
        this.sslOnly = sslOnly;
        this.excludeCipherSuites = Collections.unmodifiableList(excludeCipherSuites);
    }

    public static class Builder {
        protected File keyStoreFile;
        protected String keyStorePassword;
        protected Integer sslPort;
        protected Boolean sslOnly;
        protected List<String> excludeCipherSuites = new ArrayList<String>();

        public Builder withKeyStoreFile(File keyStoreFile) {
            this.keyStoreFile = keyStoreFile;
            return this;
        }

        public Builder withKeyStorePassword(String keyStorePassword) {
            this.keyStorePassword = keyStorePassword;
            return this;
        }

        public Builder withSslPort(Integer sslPort) {
            this.sslPort = sslPort;
            return this;
        }

        public Builder withSslOnly(Boolean sslOnly) {
            this.sslOnly = sslOnly;
            return this;
        }

        public Builder withExcludedCipherSuite(String cipherSuite) {
            excludeCipherSuites.add(cipherSuite);
            return this;
        }

        public SSLConfiguration build() {
            if (keyStoreFile == null) {
                throw new IllegalArgumentException("keystore file has not been specified.");
            }
            if (keyStorePassword == null) {
                throw new IllegalArgumentException("keystore password has not been specified.");
            }
            if (sslPort == null) {
                throw new IllegalArgumentException("SSL port has not been specified.");
            }
            return new SSLConfiguration(keyStoreFile, keyStorePassword, sslPort, sslOnly, excludeCipherSuites);
        }
    }
}
