package com.apiforge.backend.engine.manager;

import com.apiforge.backend.request.dto.RequestDto;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
public class ProxyManager implements ExecutionInterceptor {

    @Override
    public void preProcess(RequestDto request) {
        // TODO: Inspect request metadata to see if a proxy is configured (e.g., SOCKS5, HTTP)
        // If so, attach proxy metadata to the request execution context.
    }
}
