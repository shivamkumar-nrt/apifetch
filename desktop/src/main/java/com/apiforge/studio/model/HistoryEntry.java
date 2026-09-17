package com.apiforge.studio.model;

import java.util.UUID;

@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class HistoryEntry {
    private String id;
    private ApiRequest request;
    private long timestamp;
    private int statusCode;
    private long responseTimeMs;

    public HistoryEntry() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
    }

    public HistoryEntry(ApiRequest request, int statusCode, long responseTimeMs) {
        this.id = UUID.randomUUID().toString();
        this.request = request;
        this.statusCode = statusCode;
        this.responseTimeMs = responseTimeMs;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public ApiRequest getRequest() { return request; }
    public void setRequest(ApiRequest request) { this.request = request; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public int getStatusCode() { return statusCode; }
    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }

    public long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(long responseTimeMs) { this.responseTimeMs = responseTimeMs; }
}
