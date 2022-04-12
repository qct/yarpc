package org.yarpc.core;

/**
 * <p>Created by qdd on 2022/4/11.
 */
public interface YaRpcConstant {

    int DEFAULT_WORKER_THREADS = Math.min(Runtime.getRuntime().availableProcessors() + 1, 32);
}
