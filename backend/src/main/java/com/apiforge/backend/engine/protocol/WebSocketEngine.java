package com.apiforge.backend.engine.protocol;

import com.apiforge.backend.engine.core.ExecutionEngine;
import com.apiforge.backend.engine.dto.ResponseDto;
import com.apiforge.backend.request.dto.RequestDto;
import org.springframework.stereotype.Component;

@Component
public class WebSocketEngine implements ExecutionEngine {

    @Override
    public boolean supports(String protocol) {
        return "websocket".equalsIgnoreCase(protocol) || "ws".equalsIgnoreCase(protocol) || "wss".equalsIgnoreCase(protocol);
    }

    @Override
    public ResponseDto execute(RequestDto request) throws Exception {
        // TODO: Implement WS persistent connection logic
        throw new UnsupportedOperationException("WebSocket execution not yet fully implemented");
    }
}
