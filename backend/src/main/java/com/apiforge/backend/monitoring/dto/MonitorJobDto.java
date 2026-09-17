package com.apiforge.backend.monitoring.dto;

import java.util.UUID;

public class MonitorJobDto {
    private UUID id;
    private UUID workspaceId;
    private String name;
    private String url;
    private String method;
    private int intervalSeconds;
    private boolean isActive;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(UUID workspaceId) { this.workspaceId = workspaceId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public int getIntervalSeconds() { return intervalSeconds; }
    public void setIntervalSeconds(int intervalSeconds) { this.intervalSeconds = intervalSeconds; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
