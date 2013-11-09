package com.xoom.oss.feathercon;

import java.io.File;

public class SSLConfiguration {
    public final File keyStoreFile;
    public final String keyStorePassword;
    public final Integer sslPort;
    public final Boolean sslOnly;

    private SSLConfiguration(File keyStoreFile, String keyStorePassword, Integer sslPort, Boolean sslOnly) {
        this.keyStoreFile = keyStoreFile;
        this.keyStorePassword = keyStorePassword;
        this.sslPort = sslPort;
        this.sslOnly = sslOnly == null ? false : sslOnly;
    }

    public static class Builder {
        protected File keyStoreFile;
        protected String keyStorePassword;
        protected Integer sslPort;
        protected Boolean sslOnly;

        Builder withKeyStoreFile(File keyStoreFile) {
            this.keyStoreFile = keyStoreFile;
            return this;
        }

        Builder withKeyStorePassword(String keyStorePassword) {
            this.keyStorePassword = keyStorePassword;
            return this;
        }

        Builder withSslPort(Integer sslPort) {
            this.sslPort = sslPort;
            return this;
        }

        Builder withSslOnly(Boolean sslOnly) {
            this.sslOnly = sslOnly;
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
            return new SSLConfiguration(keyStoreFile, keyStorePassword, sslPort, sslOnly);
        }
    }
}
