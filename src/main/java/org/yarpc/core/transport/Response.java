package org.yarpc.core.transport;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.yarpc.core.exception.YaRpcExceptionWrapper;

/**
 * <p>Created by qdd on 2022/4/10.
 */
@Getter
@Setter
@NoArgsConstructor
public class Response {

    private long reqId;
    private Object resp;
    private YaRpcExceptionWrapper e;

    public Response(long reqId, Object resp) {
        this.reqId = reqId;
        this.resp = resp;
    }

    public Response(long reqId) {
        this.reqId = reqId;
    }
}
