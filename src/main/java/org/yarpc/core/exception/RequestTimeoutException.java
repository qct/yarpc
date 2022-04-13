package org.yarpc.core.exception;

/**
 * <p>Created by qdd on 2022/4/10.
 */
public final class RequestTimeoutException extends ClientSideException {

    public RequestTimeoutException(String message) {
        super(message);
    }
}
