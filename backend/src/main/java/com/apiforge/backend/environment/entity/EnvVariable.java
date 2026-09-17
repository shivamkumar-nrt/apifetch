package com.apiforge.backend.environment.entity;

import com.apiforge.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "env_variables", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"environment_id", "key_name"})
})
public class EnvVariable extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "environment_id", nullable = false)
    private Environment environment;

    @Column(name = "key_name", nullable = false, length = 150)
    private String keyName;

    @Column(name = "value_encrypted")
    private byte[] valueEncrypted;

    @Column(name = "is_secret", nullable = false)
    private boolean isSecret = false;

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Environment getEnvironment() { return environment; }
    public void setEnvironment(Environment environment) { this.environment = environment; }
    public String getKeyName() { return keyName; }
    public void setKeyName(String keyName) { this.keyName = keyName; }
    public byte[] getValueEncrypted() { return valueEncrypted; }
    public void setValueEncrypted(byte[] valueEncrypted) { this.valueEncrypted = valueEncrypted; }
    public boolean isSecret() { return isSecret; }
    public void setSecret(boolean isSecret) { this.isSecret = isSecret; }
}
