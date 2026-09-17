CREATE TABLE collections (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(255),
    version INT,
    deleted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_col_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces (id),
    CONSTRAINT uq_col_ws_name UNIQUE (workspace_id, name)
);

CREATE TABLE requests (
    id UUID PRIMARY KEY,
    collection_id UUID NOT NULL,
    folder_id UUID,
    name VARCHAR(200) NOT NULL,
    method VARCHAR(10) NOT NULL DEFAULT 'GET',
    url TEXT NOT NULL,
    headers JSON DEFAULT '{}',
    body JSON,
    body_type VARCHAR(50),
    form_data JSON,
    request_settings JSON,
    protocol VARCHAR(20) NOT NULL DEFAULT 'rest',
    pre_request_script TEXT,
    test_script TEXT,
    order_index INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(255),
    version INT,
    deleted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_req_collection FOREIGN KEY (collection_id) REFERENCES collections (id)
);

CREATE TABLE environments (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(255),
    version INT,
    deleted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_env_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces (id),
    CONSTRAINT uq_env_ws_name UNIQUE (workspace_id, name)
);

CREATE TABLE env_variables (
    id UUID PRIMARY KEY,
    environment_id UUID NOT NULL,
    key_name VARCHAR(150) NOT NULL,
    value_encrypted BYTEA,
    is_secret BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(255),
    version INT,
    CONSTRAINT fk_ev_environment FOREIGN KEY (environment_id) REFERENCES environments (id),
    CONSTRAINT uq_ev_env_key UNIQUE (environment_id, key_name)
);

CREATE TABLE test_results (
    id UUID PRIMARY KEY,
    request_id UUID NOT NULL,
    execution_id UUID NOT NULL,
    assertion_type VARCHAR(50) NOT NULL,
    passed BOOLEAN NOT NULL,
    expected TEXT,
    actual TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(255),
    version INT,
    CONSTRAINT fk_tr_request FOREIGN KEY (request_id) REFERENCES requests (id)
);

