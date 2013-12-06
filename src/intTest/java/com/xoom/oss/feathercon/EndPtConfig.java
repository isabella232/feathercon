package com.xoom.oss.feathercon;

import javax.websocket.server.ServerEndpointConfig;

public class EndPtConfig extends ServerEndpointConfig.Configurator {
    @Override
    public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
        return super.getEndpointInstance(endpointClass);
    }
}
