package com.apiforge.backend.engine.core;

import com.apiforge.backend.engine.dto.ResponseDto;
import com.apiforge.backend.request.dto.RequestDto;

/**
 * Base interface for all protocol execution engines (REST, GraphQL, WebSocket, etc.).
 */
public interface ExecutionEngine {
    
    /**
     * Executes the given request.
     *
     * @param request The request to execute.
     * @return The response.
     */
    ResponseDto execute(RequestDto request) throws Exception;
    
    /**
     * @return True if this engine supports the given protocol string (e.g., "rest", "graphql").
     */
    boolean supports(String protocol);
}
