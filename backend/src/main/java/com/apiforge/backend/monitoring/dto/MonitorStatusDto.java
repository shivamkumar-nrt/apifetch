package com.apiforge.backend.monitoring.dto;

import java.time.ZonedDateTime;
import java.util.UUID;

public class MonitorStatusDto {
    private UUID id;
    private UUID jobId;
    private boolean isUp;
    private int statusCode;
    private long responseTimeMs;
    private ZonedDateTime checkedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getJobId() { return jobId; }
    public void setJobId(UUID jobId) { this.jobId = jobId; }
    public boolean isUp() { return isUp; }
    public void setUp(boolean up) { isUp = up; }
    public int getStatusCode() { return statusCode; }
    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }
    public long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(long responseTimeMs) { this.responseTimeMs = responseTimeMs; }
    public ZonedDateTime getCheckedAt() { return checkedAt; }
    public void setCheckedAt(ZonedDateTime checkedAt) { this.checkedAt = checkedAt; }
}
