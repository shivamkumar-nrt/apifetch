package com.apiforge.backend.engine.manager;

import com.apiforge.backend.request.dto.RequestDto;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class AuthEngine implements ExecutionInterceptor {

    @Override
    public void preProcess(RequestDto request) {
        String authType = request.getAuthType();
        String authConfig = request.getAuthConfig(); // Assume JSON or direct string

        if (authType == null || authConfig == null) {
            return;
        }

        String existingHeaders = request.getHeaders();
        // Simplified auth injection logic (in reality, parse JSON)
        if ("BEARER".equalsIgnoreCase(authType)) {
            String newHeader = "\"Authorization\": \"Bearer " + authConfig + "\"";
            if (existingHeaders == null || existingHeaders.equals("{}")) {
                request.setHeaders("{" + newHeader + "}");
            } else {
                request.setHeaders(existingHeaders.replace("}", "," + newHeader + "}"));
            }
        } else if ("BASIC".equalsIgnoreCase(authType)) {
            // Assume authConfig is already base64 encoded "username:password"
            String newHeader = "\"Authorization\": \"Basic " + authConfig + "\"";
            if (existingHeaders == null || existingHeaders.equals("{}")) {
                request.setHeaders("{" + newHeader + "}");
            } else {
                request.setHeaders(existingHeaders.replace("}", "," + newHeader + "}"));
            }
        }
    }
}
