package org.yarpc;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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
    private String ip = "127.0.0.1";
    private int port = 8081;

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
    void init() {
        Client client = new ClientImpl(ip, port);
        Hello hello = client.proxyInstance(Hello.class);
        String alex = hello.sayHi("alex");
        System.out.println("=====================" + alex);
    }
}
