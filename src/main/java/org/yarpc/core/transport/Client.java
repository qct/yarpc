package org.yarpc.core.transport;

import java.lang.reflect.Method;

/**
 * <p>Created by qdd on 2022/4/10.
 */
public interface Client {

    Response sendMessage(Class<?> clazz, Method method, Object[] args);

    <T> T proxyInstance(Class<T> serviceInterface);

}
