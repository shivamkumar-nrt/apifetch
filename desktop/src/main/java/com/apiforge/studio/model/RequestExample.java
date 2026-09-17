package com.apiforge.studio.model;

import java.util.Map;
import java.util.HashMap;

public class RequestExample {
    private int id;
    private int requestId;
    private String name;
    private int statusCode;
    private String responseBody;
    private Map<String, String> responseHeaders;

    public RequestExample(int id, int requestId, String name, int statusCode, String responseBody) {
        this.id = id;
        this.requestId = requestId;
        this.name = name;
        this.statusCode = statusCode;
        this.responseBody = responseBody;
        this.responseHeaders = new HashMap<>();
    }

    public int getId() { return id; }
    public int getRequestId() { return requestId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public int getStatusCode() { return statusCode; }
    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }
    
    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }
    
    public Map<String, String> getResponseHeaders() { return responseHeaders; }
    public void setResponseHeaders(Map<String, String> responseHeaders) { this.responseHeaders = responseHeaders; }

    @Override
    public String toString() {
        return name;
    }
}
