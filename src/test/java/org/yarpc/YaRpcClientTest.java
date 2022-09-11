package org.yarpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.yarpc.core.exception.YaRpcException;
import org.yarpc.core.transport.Client;
import org.yarpc.core.transport.ClientImpl;
import org.yarpc.core.transport.Server;
import org.yarpc.core.transport.ServerImpl;
import org.yarpc.support.Hello;
import org.yarpc.support.HelloImpl;

/**
 * <p>Created by qdd on 2022/4/10.
 */
public class YaRpcClientTest {

    private static Server server;
    private final String ip = "127.0.0.1";
    private final int port = 8081;

    @BeforeAll
    static void beforeAll() {
        server = new ServerImpl(8081, "sayHi", new HelloImpl());
        server.start();
    }

    @AfterAll
    static void afterAll() {
        server.shutdown();
    }

    @Test
    void rpcCallWithException() {
        Client client = new ClientImpl(ip, port);
        Hello hello = client.proxyInstance(Hello.class);
        assertThatExceptionOfType(YaRpcException.class)
                .isThrownBy(() -> hello.sayHi("alex"))
                .withMessage("hello Exception");
    }

    @Test
    void rpcCallShouldSuccess() {
        Client client = new ClientImpl(ip, port);
        Hello hello = client.proxyInstance(Hello.class);
        String hiJohn = hello.sayHi("John");
        assertThat(hiJohn).isEqualTo("hello John");
    }
}
