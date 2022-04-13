package org.yarpc.core.transport.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.yarpc.core.codec.ProtocolDecoder;
import org.yarpc.core.codec.ProtocolEncoder;
import org.yarpc.core.exception.ServerSideException;
import org.yarpc.core.serializer.KryoSerializer;
import org.yarpc.core.transport.Request;
import org.yarpc.core.transport.Response;
import org.yarpc.support.Hello;
import org.yarpc.support.HelloImpl;

/**
 * <p>Created by qdd on 2022/4/16.
 */
class YaRpcServerHandlerTest {

    private EmbeddedChannel ch;

    @BeforeEach
    void setUp() {
        ch = new EmbeddedChannel(new ProtocolDecoder(new KryoSerializer()),
            new ProtocolEncoder(new KryoSerializer()), new YaRpcServerHandler(new HelloImpl()));
    }

    @Test
    void writeFailedResponse() throws NoSuchMethodException, InterruptedException {
        ch.writeInbound(new Request(1L, Hello.class, "noSuchMethodException", new Class[]{String.class},
            new String[]{"Alex"}, System.currentTimeMillis()));

        while (ch.outboundMessages().size() == 0) {
            Thread.sleep(50);
        }
        ByteBuf buf = ch.readOutbound();
        ch.writeInbound(buf);
        Response resp = ch.readInbound();
        Assertions.assertThat(resp.getE().toException()).isInstanceOf(ServerSideException.class)
            .hasMessage("org.yarpc.support.HelloImpl.noSuchMethodException(java.lang.String)");
    }

    @Test
    void writeSuccessResponse() throws NoSuchMethodException, InterruptedException {
        ch.writeInbound(new Request(1L, Hello.class, "sayHi", new Class[]{String.class},
            new String[]{"Alex"}, System.currentTimeMillis()));
        while (ch.outboundMessages().size() == 0) {
            Thread.sleep(50);
        }
        ByteBuf buf = ch.readOutbound();
        ch.writeInbound(buf);
        Response resp = ch.readInbound();
        Assertions.assertThat(resp.getResp()).isEqualTo("hello Alex");
    }
}