package com.apiforge.studio.model;

import java.util.ArrayList;
import java.util.List;

public class Environment {
    private String id;
    private int workspaceId;
    private String name;
    private boolean isDefault;
    private List<EnvVariable> variables;

    public Environment() {
        this.variables = new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public int getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(int workspaceId) { this.workspaceId = workspaceId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }
    public List<EnvVariable> getVariables() { return variables; }
    public void setVariables(List<EnvVariable> variables) { this.variables = variables; }

    @Override
    public String toString() {
        return name != null ? name : "Unknown Environment";
    }
}
