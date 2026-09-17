package com.apiforge.backend.monitoring.entity;

import jakarta.persistence.*;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "monitor_status")
public class MonitorStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "job_id", nullable = false)
    private UUID jobId;

    @Column(name = "status_code", nullable = false)
    private Integer statusCode;

    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    @Column(name = "is_up", nullable = false)
    private Boolean isUp;

    @Column(name = "checked_at", nullable = false)
    private ZonedDateTime checkedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getJobId() { return jobId; }
    public void setJobId(UUID jobId) { this.jobId = jobId; }
    public Integer getStatusCode() { return statusCode; }
    public void setStatusCode(Integer statusCode) { this.statusCode = statusCode; }
    public Long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(Long responseTimeMs) { this.responseTimeMs = responseTimeMs; }
    public Boolean getUp() { return isUp; }
    public void setUp(Boolean up) { isUp = up; }
    public ZonedDateTime getCheckedAt() { return checkedAt; }
    public void setCheckedAt(ZonedDateTime checkedAt) { this.checkedAt = checkedAt; }
}
