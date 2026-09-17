package com.apiforge.backend.engine.entity;

import com.apiforge.backend.common.entity.BaseEntity;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "test_results")
public class TestResult extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "request_id", nullable = false)
    private UUID requestId;

    @Column(name = "execution_id", nullable = false)
    private UUID executionId;

    @Column(name = "assertion_type", nullable = false)
    private String assertionType;

    @Column(nullable = false)
    private Boolean passed;

    @Column(columnDefinition = "TEXT")
    private String expected;

    @Column(columnDefinition = "TEXT")
    private String actual;

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getRequestId() { return requestId; }
    public void setRequestId(UUID requestId) { this.requestId = requestId; }
    public UUID getExecutionId() { return executionId; }
    public void setExecutionId(UUID executionId) { this.executionId = executionId; }
    public String getAssertionType() { return assertionType; }
    public void setAssertionType(String assertionType) { this.assertionType = assertionType; }
    public Boolean getPassed() { return passed; }
    public void setPassed(Boolean passed) { this.passed = passed; }
    public String getExpected() { return expected; }
    public void setExpected(String expected) { this.expected = expected; }
    public String getActual() { return actual; }
    public void setActual(String actual) { this.actual = actual; }
}
