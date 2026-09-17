package com.apiforge.backend.environment.dto;

import java.util.UUID;

public class EnvVariableDto {
    private UUID id;
    private String keyName;
    private String value; // Decrypted value for client or placeholder if secret
    private boolean isSecret;

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getKeyName() { return keyName; }
    public void setKeyName(String keyName) { this.keyName = keyName; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public boolean isSecret() { return isSecret; }
    public void setSecret(boolean isSecret) { this.isSecret = isSecret; }
}
