package org.yarpc.core.transport;

import com.google.common.base.Preconditions;
import com.google.common.reflect.AbstractInvocationHandler;
import com.google.common.reflect.Reflection;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.CheckForNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yarpc.core.YaRpcConstant;
import org.yarpc.core.exception.YaRpcException;

/**
 * <p>Created by qdd on 2022/4/10.
 */
public class ClientImpl implements Client {

    public static final ConcurrentMap<Long, CompletableFuture<Response>> RESPONSE_HOLDER = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(ClientImpl.class);
    private static final AtomicLong REQUEST_ID = new AtomicLong();
    private final Transporter transporter;
    private final long requestTimeout;

    public ClientImpl(String ip, int port) {
        this.requestTimeout = YaRpcConstant.CLIENT_TIMEOUT_MILLIS;
        this.transporter = new NettyClientTransporter(ip, port);
    }

    @Override
    public Response sendMessage(Class<?> clazz, Method method, Object[] args) {
        Preconditions.checkNotNull(clazz);
        Preconditions.checkNotNull(method);
        Request request = new Request(
                REQUEST_ID.incrementAndGet(),
                clazz,
                method.getName(),
                method.getParameterTypes(),
                args,
                System.currentTimeMillis());
        return transporter.sendToRemote(request, this.requestTimeout);
    }

    @Override
    public <T> T proxyInstance(Class<T> serviceInterface) {
        return Reflection.newProxy(serviceInterface, new AbstractInvocationHandler() {
            @CheckForNull
            @Override
            protected Object handleInvocation(Object proxy, Method method, @Nullable Object[] args) throws Throwable {
                try {
                    return sendMessage(serviceInterface, method, args).getResp();
                } catch (YaRpcException e) {
                    logger.error("Exception while calling [{}.{}(...)]", serviceInterface.getName(), method.getName());
                    throw e;
                }
            }
        });
    }
}
