CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    full_name VARCHAR(150) NOT NULL,
    status VARCHAR(50) NOT NULL,
    mfa_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    last_login_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(255),
    version INT,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE organizations (
    id UUID PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE,
    plan_tier VARCHAR(50) NOT NULL,
    owner_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(255),
    version INT,
    deleted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_org_owner FOREIGN KEY (owner_id) REFERENCES users (id)
);

CREATE TABLE workspaces (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    name VARCHAR(150) NOT NULL,
    visibility VARCHAR(50) NOT NULL,
    owner_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(255),
    version INT,
    deleted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_ws_org FOREIGN KEY (organization_id) REFERENCES organizations (id),
    CONSTRAINT fk_ws_owner FOREIGN KEY (owner_id) REFERENCES users (id),
    CONSTRAINT uq_ws_org_name UNIQUE (organization_id, name)
);

CREATE TABLE user_org_roles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    organization_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(255),
    version INT,
    CONSTRAINT fk_uor_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_uor_org FOREIGN KEY (organization_id) REFERENCES organizations (id),
    CONSTRAINT uq_uor_user_org UNIQUE (user_id, organization_id)
);

CREATE TABLE user_workspace_roles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    workspace_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(255),
    version INT,
    CONSTRAINT fk_uwr_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_uwr_ws FOREIGN KEY (workspace_id) REFERENCES workspaces (id),
    CONSTRAINT uq_uwr_user_ws UNIQUE (user_id, workspace_id)
);
