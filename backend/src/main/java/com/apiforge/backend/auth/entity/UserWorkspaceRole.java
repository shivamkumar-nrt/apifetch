package com.apiforge.backend.auth.entity;

import com.apiforge.backend.common.entity.BaseEntity;
import com.apiforge.backend.workspace.entity.Workspace;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "user_workspace_roles", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "workspace_id"})
})
public class UserWorkspaceRole extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkspaceRole role;

    public enum WorkspaceRole {
        ADMIN, EDITOR, VIEWER
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Workspace getWorkspace() { return workspace; }
    public void setWorkspace(Workspace workspace) { this.workspace = workspace; }
    public WorkspaceRole getRole() { return role; }
    public void setRole(WorkspaceRole role) { this.role = role; }
}
