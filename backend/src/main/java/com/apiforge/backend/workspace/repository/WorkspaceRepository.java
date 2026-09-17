package com.apiforge.backend.workspace.repository;

import com.apiforge.backend.workspace.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {
    Optional<Workspace> findByOrganizationIdAndName(UUID organizationId, String name);
    List<Workspace> findByOrganizationId(UUID organizationId);
    List<Workspace> findByOwnerId(UUID ownerId);
}
