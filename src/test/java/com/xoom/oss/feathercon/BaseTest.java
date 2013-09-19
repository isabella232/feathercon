package com.xoom.oss.feathercon;

import org.junit.BeforeClass;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class BaseTest {
    @BeforeClass
    public static void beforeClass() {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }
}
