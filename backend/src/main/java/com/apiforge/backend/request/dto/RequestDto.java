package com.apiforge.backend.request.dto;

import java.util.UUID;

public class RequestDto {
    private UUID id;
    private UUID collectionId;
    private UUID folderId;
    private String name;
    private String method;
    private String url;
    private String headers; // JSON string
    private String body; // JSON string
    private String bodyType; // e.g. "raw", "form-data", "urlencoded"
    private String formData; // JSON string
    private String requestSettings; // JSON string
    private String protocol;
    private String preRequestScript;
    private String testScript;
    private int orderIndex;
    
    // Engine specific properties (passed during execution but maybe not stored in DB)
    private String authType; // e.g. "BEARER", "BASIC"
    private String authConfig; // JSON config for auth

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCollectionId() { return collectionId; }
    public void setCollectionId(UUID collectionId) { this.collectionId = collectionId; }
    public UUID getFolderId() { return folderId; }
    public void setFolderId(UUID folderId) { this.folderId = folderId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getHeaders() { return headers; }
    public void setHeaders(String headers) { this.headers = headers; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getBodyType() { return bodyType; }
    public void setBodyType(String bodyType) { this.bodyType = bodyType; }
    public String getFormData() { return formData; }
    public void setFormData(String formData) { this.formData = formData; }
    public String getRequestSettings() { return requestSettings; }
    public void setRequestSettings(String requestSettings) { this.requestSettings = requestSettings; }
    public String getProtocol() { return protocol; }
    public void setProtocol(String protocol) { this.protocol = protocol; }
    public String getPreRequestScript() { return preRequestScript; }
    public void setPreRequestScript(String preRequestScript) { this.preRequestScript = preRequestScript; }
    public String getTestScript() { return testScript; }
    public void setTestScript(String testScript) { this.testScript = testScript; }
    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
    public String getAuthType() { return authType; }
    public void setAuthType(String authType) { this.authType = authType; }
    public String getAuthConfig() { return authConfig; }
    public void setAuthConfig(String authConfig) { this.authConfig = authConfig; }
}
