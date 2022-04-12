package org.yarpc.core.transport;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * <p>Created by qdd on 2022/4/10.
 */
public class ResponseHolder {

    private static final ConcurrentMap<Long, CompletableFuture<Response>> RESPONSE_MAP = new ConcurrentHashMap<>();

    public static void put(Long requestId, CompletableFuture<Response> future) {
        RESPONSE_MAP.put(requestId, future);
    }

    public static void remove(long requestId) {
        RESPONSE_MAP.remove(requestId);
    }

    public static CompletableFuture<Response> get(long requestId) {
        return RESPONSE_MAP.get(requestId);
    }
}
