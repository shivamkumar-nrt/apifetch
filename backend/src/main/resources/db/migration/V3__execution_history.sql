CREATE TABLE execution_history (
    id UUID PRIMARY KEY,
    request_id UUID,
    workspace_id UUID NOT NULL,
    url TEXT NOT NULL,
    method VARCHAR(10) NOT NULL,
    status_code INT NOT NULL,
    response_time_ms BIGINT,
    response_size_bytes BIGINT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(255),
    version INT
);
