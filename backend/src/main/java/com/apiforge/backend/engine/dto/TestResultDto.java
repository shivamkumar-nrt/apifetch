package com.apiforge.backend.engine.dto;

import java.util.UUID;

public class TestResultDto {
    private UUID id;
    private UUID requestId;
    private UUID executionId;
    private String assertionType;
    private Boolean passed;
    private String expected;
    private String actual;

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
