package org.yarpc.core.codec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.yarpc.core.serializer.KryoSerializer;

/**
 * <p>Created by qdd on 2022/4/15.
 */
class ProtocolDecoderTest {

    @Test
    void decodeShouldSuccess() throws Exception {
        KryoSerializer mockSerializer = mock(KryoSerializer.class);
        ProtocolDecoder decoder = spy(new ProtocolDecoder(mockSerializer));
        EmbeddedChannel ch = new EmbeddedChannel(decoder);
        ByteBuf buf = Unpooled.buffer();
        buf.writeInt(1); // 4 bytes header
        buf.writeCharSequence("A", StandardCharsets.UTF_8);

        when(mockSerializer.deserialize(new byte[65])).thenReturn("A");
        ch.writeInbound(buf);
        verify(decoder, times(1)).decode(any(), any());
    }

    @Test
    void decodeShouldReturnNull() throws Exception {
        ProtocolDecoder decoder = new ProtocolDecoder(new KryoSerializer());
        Object decode = decoder.decode(mock(ChannelHandlerContext.class), mock(ByteBuf.class));
        assertThat(decode).isNull();
    }
}
