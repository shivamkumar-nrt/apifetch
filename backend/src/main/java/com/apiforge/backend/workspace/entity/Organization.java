package com.apiforge.backend.workspace.entity;

import com.apiforge.backend.auth.entity.User;
import com.apiforge.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "organizations")
public class Organization extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_tier", nullable = false)
    private PlanTier planTier = PlanTier.FREE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "deleted_at")
    private ZonedDateTime deletedAt;

    public enum PlanTier {
        FREE, PRO, BUSINESS, ENTERPRISE
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public PlanTier getPlanTier() { return planTier; }
    public void setPlanTier(PlanTier planTier) { this.planTier = planTier; }
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
    public ZonedDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(ZonedDateTime deletedAt) { this.deletedAt = deletedAt; }
}
