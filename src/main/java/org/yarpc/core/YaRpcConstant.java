package org.yarpc.core;

/**
 * <p>Created by qdd on 2022/4/11.
 */
public interface YaRpcConstant {

    int DEFAULT_IO_THREADS = Math.min(Runtime.getRuntime().availableProcessors() + 1, 32);
    int DEFAULT_BIZ_THREADS = 162;

    String SERVER_BOSS_POOL_NAME = "yaRpc-server-boss-pool-%d";
    String SERVER_WORKER_POOL_NAME = "yaRpc-server-worker-pool-%d";
    String SERVER_BIZ_POOL_NAME = "yaRpc-server-biz-pool-%d";
    String CLIENT_WORKER_POOL_NAME = "yaRpc-client-worker-pool-%d";
    int CLIENT_TIMEOUT_MILLIS = 100 * 1000; // default 10seconds
    int SERVER_TIMEOUT_MILLIS = 100 * 1000; // default 10seconds
    int MAX_FRAME_LENGTH = 10 * 1024 * 1024; // default 10M
}
