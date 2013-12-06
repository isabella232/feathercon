package com.xoom.oss.feathercon;

import javax.websocket.Decoder;
import javax.websocket.Encoder;
import javax.websocket.Extension;
import javax.websocket.server.ServerEndpointConfig;
import java.util.List;
import java.util.Map;

public class CFG implements ServerEndpointConfig {
    @Override
    public Class<?> getEndpointClass() {
        return null;
    }

    @Override
    public String getPath() {
        return null;
    }

    @Override
    public List<String> getSubprotocols() {
        return null;
    }

    @Override
    public List<Extension> getExtensions() {
        return null;
    }

    @Override
    public Configurator getConfigurator() {
        return null;
    }

    @Override
    public List<Class<? extends Encoder>> getEncoders() {
        return null;
    }

    @Override
    public List<Class<? extends Decoder>> getDecoders() {
        return null;
    }

    @Override
    public Map<String, Object> getUserProperties() {
        return null;
    }

    public static class Cator extends Configurator {

    }
}
