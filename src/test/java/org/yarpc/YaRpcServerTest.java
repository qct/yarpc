package org.yarpc;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.yarpc.core.transport.Server;
import org.yarpc.core.transport.ServerImpl;
import org.yarpc.support.HelloImpl;

/**
 * <p>Created by qdd on 2022/4/10.
 */
public class YaRpcServerTest {

    private static Server server;

    @BeforeEach
    void setUp() {
        server = new ServerImpl(8081, "sayHi", new HelloImpl());
    }

    @Test
    void startServer() {
        server.start();
        Assertions.assertThat(server.isStarted()).isTrue();
    }

    @AfterEach
    void tearDown() {
        server.shutdown();
    }
}
