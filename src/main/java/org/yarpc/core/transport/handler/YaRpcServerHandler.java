package org.yarpc.core.transport.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yarpc.core.YaRpcConstant;
import org.yarpc.core.transport.Request;
import org.yarpc.core.transport.Response;

/**
 * <p>Created by qdd on 2022/4/10.
 */
public class YaRpcServerHandler extends SimpleChannelInboundHandler<Request> {

    private static final Logger logger = LoggerFactory.getLogger(YaRpcServerHandler.class);

    private final Object service;

    public YaRpcServerHandler(Object serviceImpl) {
        this.service = serviceImpl;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Request msg) throws Exception {
        String methodName = msg.getMethod();
        Object[] params = msg.getParams();
        Class<?>[] parameterTypes = msg.getParameterTypes();
        long requestId = msg.getRequestId();
        Method method = service.getClass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        Object invoked = method.invoke(service, params);
        Response response = Response.builder()
            .requestId(requestId)
            .response(invoked)
            .build();
        ctx.pipeline().writeAndFlush(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Exception caught on {}, ", ctx.channel(), cause);
        ctx.channel().close();
    }
}
