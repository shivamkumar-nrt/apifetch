package com.apiforge.backend.engine.dto;

import java.util.Map;

public class ResponseDto {
    private int statusCode;
    private String statusText;
    private long responseTimeMs;
    private long responseSizeBytes;
    private Map<String, String> headers;
    private String body; // String representation of the response body

    // Getters and Setters
    public int getStatusCode() { return statusCode; }
    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }
    public String getStatusText() { return statusText; }
    public void setStatusText(String statusText) { this.statusText = statusText; }
    public long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(long responseTimeMs) { this.responseTimeMs = responseTimeMs; }
    public long getResponseSizeBytes() { return responseSizeBytes; }
    public void setResponseSizeBytes(long responseSizeBytes) { this.responseSizeBytes = responseSizeBytes; }
    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
}
