package com.apiforge.studio.model;

public class EnvVariable {
    private String id;
    private int environmentId;
    private String key;
    private String value;
    private boolean isSecret;

    public EnvVariable() {}

    public EnvVariable(String key, String value, boolean isSecret) {
        this.key = key;
        this.value = value;
        this.isSecret = isSecret;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public int getEnvironmentId() { return environmentId; }
    public void setEnvironmentId(int environmentId) { this.environmentId = environmentId; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public boolean isSecret() { return isSecret; }
    public void setSecret(boolean isSecret) { this.isSecret = isSecret; }
}
