package org.yarpc.core.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yarpc.core.serializer.Serializer;

/**
 * <p>Created by qdd on 2022/4/10.
 */
public class ProtocolEncoder extends MessageToByteEncoder<Object> {

    private static final Logger logger = LoggerFactory.getLogger(ProtocolEncoder.class);

    private final Serializer serializer;

    public ProtocolEncoder(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object msg, ByteBuf out) throws Exception {
        logger.info("Encoding...");
        byte[] serialized = serializer.serialize(msg);
        int length = serialized.length;
        out.writeInt(length);
        out.writeBytes(serialized);
    }
}
