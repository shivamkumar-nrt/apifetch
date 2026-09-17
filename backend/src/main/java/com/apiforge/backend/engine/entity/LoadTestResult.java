package com.apiforge.backend.engine.entity;

import com.apiforge.backend.common.entity.BaseEntity;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "load_test_results")
public class LoadTestResult extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "workspace_id", nullable = false)
    private UUID workspaceId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(nullable = false, length = 10)
    private String method;

    @Column(name = "concurrent_users", nullable = false)
    private Integer concurrentUsers;

    @Column(name = "total_requests", nullable = false)
    private Integer totalRequests;

    @Column(name = "success_count", nullable = false)
    private Integer successCount;

    @Column(name = "failure_count", nullable = false)
    private Integer failureCount;

    @Column(name = "min_latency_ms")
    private Long minLatencyMs;

    @Column(name = "max_latency_ms")
    private Long maxLatencyMs;

    @Column(name = "avg_latency_ms")
    private Double avgLatencyMs;

    @Column(name = "p50_ms")
    private Long p50Ms;

    @Column(name = "p90_ms")
    private Long p90Ms;

    @Column(name = "p99_ms")
    private Long p99Ms;

    @Column(name = "tps")
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
    public Integer getConcurrentUsers() { return concurrentUsers; }
    public void setConcurrentUsers(Integer concurrentUsers) { this.concurrentUsers = concurrentUsers; }
    public Integer getTotalRequests() { return totalRequests; }
    public void setTotalRequests(Integer totalRequests) { this.totalRequests = totalRequests; }
    public Integer getSuccessCount() { return successCount; }
    public void setSuccessCount(Integer successCount) { this.successCount = successCount; }
    public Integer getFailureCount() { return failureCount; }
    public void setFailureCount(Integer failureCount) { this.failureCount = failureCount; }
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
