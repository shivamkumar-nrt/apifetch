package com.apiforge.studio.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class ApiRequest {
    private String id;
    private String name;
    private String url;
    private String method; // GET, POST, etc.
    private String protocol; // REST, GraphQL, WebSocket, gRPC, SOAP, SSE
    private Map<String, String> headers;
    private Map<String, String> queryParams;
    private String body;
    private String authType; // NONE, BASIC, BEARER, API_KEY
    private Map<String, String> authConfig;
    private String preRequestScript;
    private String testScript;
    private java.util.List<RequestExample> examples;
    
    // New Postman-like features
    private String bodyType; // none, form-data, urlencoded, raw, binary
    private Map<String, String> formData; // Used for both form-data and urlencoded
    private Map<String, Boolean> requestSettings;

    private static final ObjectMapper mapper = new ObjectMapper();

    public ApiRequest() {
        this.id = null;
        this.name = "New Request";
        this.url = "";
        this.method = "GET";
        this.protocol = "REST";
        this.headers = new HashMap<>();
        this.queryParams = new HashMap<>();
        this.body = "";
        this.authType = "NONE";
        this.authConfig = new HashMap<>();
        this.examples = new java.util.ArrayList<>();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCustomName() { return name; }
    public void setCustomName(String name) { this.name = name; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getProtocol() { return protocol; }
    public void setProtocol(String protocol) { this.protocol = protocol; }

    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }

    public Map<String, String> getQueryParams() { return queryParams; }
    public void setQueryParams(Map<String, String> queryParams) { this.queryParams = queryParams; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getAuthType() { return authType; }
    public void setAuthType(String authType) { this.authType = authType; }

    public Map<String, String> getAuthConfig() { return authConfig; }
    public void setAuthConfig(Map<String, String> authConfig) { this.authConfig = authConfig; }

    public java.util.List<RequestExample> getExamples() { return examples; }
    public void setExamples(java.util.List<RequestExample> examples) { this.examples = examples; }

    public String getBodyType() { return bodyType != null ? bodyType : "raw"; }
    public void setBodyType(String bodyType) { this.bodyType = bodyType; }

    public Map<String, String> getFormData() { return formData != null ? formData : new HashMap<>(); }
    public void setFormData(Map<String, String> formData) { this.formData = formData; }

    public Map<String, Boolean> getRequestSettings() { return requestSettings != null ? requestSettings : new HashMap<>(); }
    public void setRequestSettings(Map<String, Boolean> requestSettings) { this.requestSettings = requestSettings; }

    // Helpers for Database serialization
    public String getAuthUsername() { return authConfig.getOrDefault("username", ""); }
    public void setAuthUsername(String val) { authConfig.put("username", val); }

    public String getAuthPassword() { return authConfig.getOrDefault("password", ""); }
    public void setAuthPassword(String val) { authConfig.put("password", val); }

    public String getAuthToken() { return authConfig.getOrDefault("token", ""); }
    public void setAuthToken(String val) { authConfig.put("token", val); }

    public String getApiKeyName() { return authConfig.getOrDefault("keyName", ""); }
    public void setApiKeyName(String val) { authConfig.put("keyName", val); }

    public String getApiKeyValue() { return authConfig.getOrDefault("keyValue", ""); }
    public void setApiKeyValue(String val) { authConfig.put("keyValue", val); }

    public String getHeadersJson() {
        try {
            return mapper.writeValueAsString(headers);
        } catch (Exception e) {
            return "{}";
        }
    }

    public void setHeadersFromJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            this.headers = new HashMap<>();
            return;
        }
        try {
            this.headers = mapper.readValue(json, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            this.headers = new HashMap<>();
        }
    }

    public String getPreRequestScript() { return preRequestScript; }
    public void setPreRequestScript(String preRequestScript) { this.preRequestScript = preRequestScript; }
    public String getTestScript() { return testScript; }
    public void setTestScript(String testScript) { this.testScript = testScript; }

    @Override
    public String toString() {
        return "[" + getMethod() + "] " + (getName() != null && !getName().trim().isEmpty() ? getName() : getUrl());
    }
}
