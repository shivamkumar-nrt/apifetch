package com.apiforge.backend.engine.manager;

import com.apiforge.backend.request.dto.RequestDto;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class CookieManager implements ExecutionInterceptor {

    @Override
    public void preProcess(RequestDto request) {
        // TODO: Extract cookies from a virtual cookie jar tied to the workspace/environment
        // and inject the "Cookie" header into the request.
    }
}
