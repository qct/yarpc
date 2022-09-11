package org.yarpc.core.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yarpc.core.YaRpcConstant;
import org.yarpc.core.serializer.Serializer;

/**
 * <p>Created by qdd on 2022/4/10.
 */
public class ProtocolDecoder extends LengthFieldBasedFrameDecoder {

    private static final Logger logger = LoggerFactory.getLogger(ProtocolDecoder.class);

    private final Serializer serializer;
    private static final int MSG_PROTOCOL_HEADER_FIELD_LENGTH = 4;

    public ProtocolDecoder(Serializer serializer) {
        this(YaRpcConstant.MAX_FRAME_LENGTH, serializer);
    }

    public ProtocolDecoder(int maxFrameLength, Serializer serializer) {
        super(maxFrameLength, 0, MSG_PROTOCOL_HEADER_FIELD_LENGTH, 0, MSG_PROTOCOL_HEADER_FIELD_LENGTH);
        this.serializer = serializer;
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        logger.info("Decoding...");
        ByteBuf decode = (ByteBuf) super.decode(ctx, in);
        if (decode == null) {
            logger.debug("Decoder Result is null");
            return null;
        }
        int length = decode.readableBytes();
        byte[] bytes = new byte[length];
        decode.readBytes(bytes);
        return serializer.deserialize(bytes);
    }
}
