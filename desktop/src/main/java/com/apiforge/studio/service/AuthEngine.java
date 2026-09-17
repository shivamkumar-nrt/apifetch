package com.apiforge.studio.service;

import com.apiforge.studio.model.ApiRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public class AuthEngine {

    public void applyAuth(ApiRequest request, Map<String, String> resolvedHeaders) {
        String authType = request.getAuthType();
        Map<String, String> config = request.getAuthConfig();

        if (authType == null || "NONE".equals(authType) || config == null) {
            return;
        }

        switch (authType) {
            case "BASIC":
                String username = config.getOrDefault("username", "");
                String password = config.getOrDefault("password", "");
                String credentials = username + ":" + password;
                String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
                resolvedHeaders.put("Authorization", "Basic " + encoded);
                break;
            case "BEARER":
                String token = config.getOrDefault("token", "");
                resolvedHeaders.put("Authorization", "Bearer " + token);
                break;
            case "API_KEY":
                String key = config.getOrDefault("key", "");
                String value = config.getOrDefault("value", "");
                if (!key.isEmpty()) {
                    resolvedHeaders.put(key, value); // Standard approach places it in header
                }
                break;
            default:
                break;
        }
    }
}
