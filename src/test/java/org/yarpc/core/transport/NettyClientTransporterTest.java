package org.yarpc.core.transport;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.netty.channel.Channel;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.yarpc.core.YaRpcConstant;
import org.yarpc.core.exception.ClientSideException;
import org.yarpc.core.exception.RequestTimeoutException;
import org.yarpc.support.Hello;

/**
 * <p>Created by qdd on 2022/4/14.
 */
class NettyClientTransporterTest {

    @Test
    void sendToRemoteWithClientSideException() {
        Transporter transporter = new NettyClientTransporter("127.0.0.1", 8081);
        Assertions.assertThatExceptionOfType(ClientSideException.class).isThrownBy(() -> {
            transporter.sendToRemote(new Request(), YaRpcConstant.CLIENT_TIMEOUT_MILLIS);
        }).withMessage("Channel is not available now");
    }

    @Test
    void sendToRemoteWithRequestTimeoutException() {
        Channel channel = mock(Channel.class);
        when(channel.remoteAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 8081));
        Transporter transporter = new NettyClientTransporter(channel);

        Assertions.assertThatExceptionOfType(RequestTimeoutException.class).isThrownBy(() -> {
            Method method = Hello.class.getDeclaredMethod("sayHi", String.class);
            transporter.sendToRemote(new Request(1L, Hello.class, method.getName(), method.getParameterTypes(),
                new String[]{"Alex"}, System.currentTimeMillis()), 1);
        }).withMessage("service org.yarpc.support.Hello.sayHi(...) timeout exceed 1 ms");
    }
}