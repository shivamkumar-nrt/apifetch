package com.apiforge.backend.engine.protocol;

import com.apiforge.backend.engine.core.ExecutionEngine;
import com.apiforge.backend.engine.dto.ResponseDto;
import com.apiforge.backend.request.dto.RequestDto;
import org.springframework.stereotype.Component;

@Component
public class GraphQlEngine implements ExecutionEngine {

    private final RestEngine restEngine;

    public GraphQlEngine(RestEngine restEngine) {
        this.restEngine = restEngine;
    }

    @Override
    public boolean supports(String protocol) {
        return "graphql".equalsIgnoreCase(protocol);
    }

    @Override
    public ResponseDto execute(RequestDto request) throws Exception {
        // GraphQL is executed as an HTTP POST request where the body contains a JSON payload 
        // with "query" and optionally "variables".
        
        request.setMethod("POST");
        
        // Ensure content-type is application/json
        String existingHeaders = request.getHeaders();
        String contentTypeHeader = "\"Content-Type\": \"application/json\"";
        if (existingHeaders == null || existingHeaders.equals("{}") || existingHeaders.trim().isEmpty()) {
            request.setHeaders("{" + contentTypeHeader + "}");
        } else if (!existingHeaders.toLowerCase().contains("content-type")) {
            request.setHeaders(existingHeaders.replace("}", "," + contentTypeHeader + "}"));
        }
        
        // We assume the frontend sends the raw GraphQL query string in request.getBody()
        // If it's already a JSON wrapper {"query": "..."}, we send it as is.
        // For this deep implementation, we assume the frontend wraps it properly.
        
        // Delegate execution to the RestEngine
        return restEngine.execute(request);
    }
}
