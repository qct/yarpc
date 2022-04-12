package org.yarpc.core.exception;

/**
 * <p>Created by qdd on 2022/4/10.
 */
public class RequestTimeoutException extends RuntimeException{

    public RequestTimeoutException(String message) {
        super(message);
    }
}
