package org.yarpc.core.transport;

/**
 * <p>Created by qdd on 2022/4/10.
 */
public interface Transporter {

    Response sendToRemote(Request request, long timeout);
}
