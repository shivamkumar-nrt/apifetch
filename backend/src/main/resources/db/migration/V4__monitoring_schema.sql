CREATE TABLE monitor_jobs (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL,
    name VARCHAR(200) NOT NULL,
    url TEXT NOT NULL,
    method VARCHAR(10) NOT NULL DEFAULT 'GET',
    interval_seconds INT NOT NULL DEFAULT 60,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(255),
    version INT
);

CREATE TABLE monitor_status (
    id UUID PRIMARY KEY,
    job_id UUID NOT NULL,
    status_code INT NOT NULL,
    response_time_ms BIGINT,
    is_up BOOLEAN NOT NULL,
    checked_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_ms_job FOREIGN KEY (job_id) REFERENCES monitor_jobs (id) ON DELETE CASCADE
);
