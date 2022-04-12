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
@NoArgsConstructor
@AllArgsConstructor
public class Response {

    private long requestId;
    private Object response;
    private Throwable throwable;
}
