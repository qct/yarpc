package org.yarpc.core.transport.handler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yarpc.core.transport.Response;
import org.yarpc.core.transport.ResponseHolder;

/**
 * <p>Created by qdd on 2022/4/10.
 */
@Sharable
public class YaRpcClientHandler extends SimpleChannelInboundHandler<Response> {

    private static final Logger logger = LoggerFactory.getLogger(YaRpcClientHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Response msg) {
        logger.info("Handle response...");
        CompletableFuture<Response> responseCompletableFuture = ResponseHolder.get(msg.getRequestId());
        if (responseCompletableFuture != null) {
            responseCompletableFuture.complete(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Exception caught on {}, ", ctx.channel(), cause);
        ctx.channel().close();
    }
}
