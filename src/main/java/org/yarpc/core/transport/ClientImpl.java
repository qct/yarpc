package org.yarpc.core.transport;

import com.google.common.reflect.AbstractInvocationHandler;
import com.google.common.reflect.Reflection;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.CheckForNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Created by qdd on 2022/4/10.
 */
public class ClientImpl implements Client {

    private static final Logger logger = LoggerFactory.getLogger(ClientImpl.class);

    private static final AtomicLong REQUEST_ID = new AtomicLong();

    private final Transporter transporter;

    public ClientImpl(String ip, int port) {
        this.transporter = new NettyClientTransporter(ip, port);
    }

    @Override
    public Response sendMessage(Class<?> clazz, Method method, Object[] args) {
        Request request = Request.builder()
            .requestId(REQUEST_ID.incrementAndGet())
            .clazz(clazz)
            .method(method.getName())
            .parameterTypes(method.getParameterTypes())
            .params(args)
            .build();
        return transporter.sendToRemote(request);
    }

    @Override
    public <T> T proxyInstance(Class<T> serviceInterface) {
        return Reflection.newProxy(serviceInterface, new AbstractInvocationHandler() {
            @CheckForNull
            @Override
            protected Object handleInvocation(Object proxy, Method method, @Nullable Object[] args) throws Throwable {
                try {
                    return sendMessage(serviceInterface, method, args).getResponse();
                } catch (Exception e) {
                    // TODO RPC invoke exception handle
                    throw new RuntimeException(e);
                }
            }
        });
    }

}
