package org.yarpc.core.exception;

/**
 * <p>Created by qdd on 2022/4/12.
 */
public final class BizException extends ServerSideException {

    public BizException(String message) {
        super(message);
    }
}
