package com.apiforge.backend.engine.dto;

import java.util.UUID;

public class LoadTestResultDto {
    private UUID id;
    private UUID workspaceId;
    private String url;
    private String method;
    private int concurrentUsers;
    private int totalRequests;
    private int successCount;
    private int failureCount;
    private Long minLatencyMs;
    private Long maxLatencyMs;
    private Double avgLatencyMs;
    private Long p50Ms;
    private Long p90Ms;
    private Long p99Ms;
    private Double tps;

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(UUID workspaceId) { this.workspaceId = workspaceId; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public int getConcurrentUsers() { return concurrentUsers; }
    public void setConcurrentUsers(int concurrentUsers) { this.concurrentUsers = concurrentUsers; }
    public int getTotalRequests() { return totalRequests; }
    public void setTotalRequests(int totalRequests) { this.totalRequests = totalRequests; }
    public int getSuccessCount() { return successCount; }
    public void setSuccessCount(int successCount) { this.successCount = successCount; }
    public int getFailureCount() { return failureCount; }
    public void setFailureCount(int failureCount) { this.failureCount = failureCount; }
    public Long getMinLatencyMs() { return minLatencyMs; }
    public void setMinLatencyMs(Long minLatencyMs) { this.minLatencyMs = minLatencyMs; }
    public Long getMaxLatencyMs() { return maxLatencyMs; }
    public void setMaxLatencyMs(Long maxLatencyMs) { this.maxLatencyMs = maxLatencyMs; }
    public Double getAvgLatencyMs() { return avgLatencyMs; }
    public void setAvgLatencyMs(Double avgLatencyMs) { this.avgLatencyMs = avgLatencyMs; }
    public Long getP50Ms() { return p50Ms; }
    public void setP50Ms(Long p50Ms) { this.p50Ms = p50Ms; }
    public Long getP90Ms() { return p90Ms; }
    public void setP90Ms(Long p90Ms) { this.p90Ms = p90Ms; }
    public Long getP99Ms() { return p99Ms; }
    public void setP99Ms(Long p99Ms) { this.p99Ms = p99Ms; }
    public Double getTps() { return tps; }
    public void setTps(Double tps) { this.tps = tps; }
}
