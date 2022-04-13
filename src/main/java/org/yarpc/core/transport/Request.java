package org.yarpc.core.transport;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * <p>Created by qdd on 2022/4/10.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Request {

    private long requestId;
    private Class<?> clazz;
    private String method;
    private Class<?>[] parameterTypes;
    private Object[] params;
    private long requestTime;
}
