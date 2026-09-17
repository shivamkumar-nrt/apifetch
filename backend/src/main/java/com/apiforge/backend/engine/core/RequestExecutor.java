package com.apiforge.backend.engine.core;

import com.apiforge.backend.engine.manager.ExecutionInterceptor;
import com.apiforge.backend.engine.dto.ResponseDto;
import com.apiforge.backend.request.dto.RequestDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RequestExecutor {

    private final List<ExecutionEngine> engines;
    private final List<ExecutionInterceptor> interceptors;
    private final com.apiforge.backend.engine.manager.HistoryManager historyManager;

    public RequestExecutor(List<ExecutionEngine> engines, List<ExecutionInterceptor> interceptors, com.apiforge.backend.engine.manager.HistoryManager historyManager) {
        this.engines = engines;
        this.interceptors = interceptors;
        this.historyManager = historyManager;
    }

    public ResponseDto execute(RequestDto request) throws Exception {
        String protocol = request.getProtocol();
        
        ExecutionEngine engine = engines.stream()
                .filter(e -> e.supports(protocol))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No execution engine found for protocol: " + protocol));

        // Pass through interceptors (Auth, Proxy, Cookie, SSL) before executing
        if (interceptors != null) {
            for (ExecutionInterceptor interceptor : interceptors) {
                interceptor.preProcess(request);
            }
        }
        
        ResponseDto response = engine.execute(request);
        
        // Log history asynchronously
        historyManager.logExecution(request, response);
        
        return response;
    }
}
