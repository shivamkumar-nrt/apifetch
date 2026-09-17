package com.apiforge.studio.model;

import java.util.HashMap;
import java.util.Map;

public class ApiResponse {
    private int statusCode;
    private String statusMessage;
    private long responseTimeMs;
    private long responseSize;
    private Map<String, String> headers;
    private String body;
    private boolean success;
    private String errorMessage;

    public ApiResponse() {
        this.headers = new HashMap<>();
        this.success = true;
    }

    public static ApiResponse error(String message) {
        ApiResponse response = new ApiResponse();
        response.setSuccess(false);
        response.setErrorMessage(message);
        return response;
    }

    // Getters and Setters
    public int getStatusCode() { return statusCode; }
    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }

    public String getStatusMessage() { return statusMessage; }
    public void setStatusMessage(String statusMessage) { this.statusMessage = statusMessage; }

    public long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(long responseTimeMs) { this.responseTimeMs = responseTimeMs; }

    public long getResponseSize() { return responseSize; }
    public void setResponseSize(long responseSize) { this.responseSize = responseSize; }

    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
