package com.apiforge.backend.environment.dto;

import java.util.UUID;
import java.util.List;

public class EnvironmentDto {
    private UUID id;
    private UUID workspaceId;
    private String name;
    private boolean isDefault;
    private List<EnvVariableDto> variables;

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(UUID workspaceId) { this.workspaceId = workspaceId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }
    public List<EnvVariableDto> getVariables() { return variables; }
    public void setVariables(List<EnvVariableDto> variables) { this.variables = variables; }
}
