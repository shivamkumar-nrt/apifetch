package com.apiforge.backend.auth.entity;

import com.apiforge.backend.common.entity.BaseEntity;
import com.apiforge.backend.workspace.entity.Organization;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "user_org_roles", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "organization_id"})
})
public class UserOrgRole extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrgRole role;

    public enum OrgRole {
        ORG_ADMIN, BILLING_ADMIN, MEMBER
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }
    public OrgRole getRole() { return role; }
    public void setRole(OrgRole role) { this.role = role; }
}
