package org.yarpc.core.transport.handler;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yarpc.core.YaRpcConstant;
import org.yarpc.core.exception.YaRpcExceptionWrapper;
import org.yarpc.core.transport.Request;
import org.yarpc.core.transport.Response;

/**
 * <p>Created by qdd on 2022/4/10.
 */
public class YaRpcServerHandler extends SimpleChannelInboundHandler<Request> {

    private static final Logger logger = LoggerFactory.getLogger(YaRpcServerHandler.class);

    private static final ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors(),
            YaRpcConstant.DEFAULT_BIZ_THREADS,
            60,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1),
            new ThreadFactoryBuilder()
                    .setNameFormat(YaRpcConstant.SERVER_BIZ_POOL_NAME)
                    .setDaemon(true)
                    .build(),
            new AbortPolicy());

    private final Object service;

    public YaRpcServerHandler(Object serviceImpl) {
        this.service = serviceImpl;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Request msg) throws Exception {
        EXECUTOR_SERVICE.execute(() -> {
            String methodName = msg.getMethod();
            Object[] params = msg.getParams();
            Class<?>[] parameterTypes = msg.getParameterTypes();
            long requestId = msg.getRequestId();
            Response response = new Response(requestId);
            try {
                Method method = service.getClass().getDeclaredMethod(methodName, parameterTypes);
                method.setAccessible(true);
                Object resp = method.invoke(service, params);
                response.setResp(resp);
            } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                logger.error(
                        "Server side error while executing [{}.{}(...)]",
                        msg.getClazz().getName(),
                        msg.getMethod());
                String eMsg;
                Class<? extends Throwable> eClass;
                if (e instanceof InvocationTargetException) {
                    eMsg = ((InvocationTargetException) e).getTargetException().getMessage();
                    eClass =
                            ((InvocationTargetException) e).getTargetException().getClass();
                } else {
                    eMsg = e.getMessage();
                    eClass = e.getClass();
                }
                response.setE(new YaRpcExceptionWrapper(eClass, eMsg));
            }
            ctx.pipeline().writeAndFlush(response);
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Exception caught on {}, ", ctx.channel(), cause);
        ctx.channel().close();
    }
}
