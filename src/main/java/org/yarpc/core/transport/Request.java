package org.yarpc.core.transport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>Created by qdd on 2022/4/10.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Request {

    private long requestId;
    private Class<?> clazz;
    private String method;
    private Class<?>[] parameterTypes;
    private Object[] params;
    private long requestTime;
}
