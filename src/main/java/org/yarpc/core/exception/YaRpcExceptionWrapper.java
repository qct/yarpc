package org.yarpc.core.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * To simplify remote Exception process, no business exception will deliver from server to client,
 * <p>instead, only exception type & message will be delivered to client side and wrapped to {@link YaRpcException}.
 * <p>Created by qdd on 2022/4/13.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public final class YaRpcExceptionWrapper {

    private Class<? extends Throwable> clazz;
    private String msg;

    public YaRpcException toException() {
        if (this.clazz == NoSuchMethodException.class || this.clazz == IllegalAccessException.class) {
            return new ServerSideException(this.msg);
        }
        return new BizException(this.msg);
    }
}
