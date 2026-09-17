package com.apiforge.backend.engine.manager;

import com.apiforge.backend.request.dto.RequestDto;

/**
 * Interceptor interface for managers that modify the request before execution.
 */
public interface ExecutionInterceptor {
    
    /**
     * Called before the request is executed by the protocol engine.
     * @param request The request to be modified/inspected.
     */
    void preProcess(RequestDto request);
}
