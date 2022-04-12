package org.yarpc.core.transport;

/**
 * <p>Created by qdd on 2022/4/10.
 */
public interface Server {

    void start();

    void shutdown();

    boolean isStarted();
}
