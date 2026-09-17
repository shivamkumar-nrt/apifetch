package com.apiforge.backend.environment.repository;

import com.apiforge.backend.environment.entity.Environment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnvironmentRepository extends JpaRepository<Environment, UUID> {
    List<Environment> findByWorkspaceId(UUID workspaceId);
    Optional<Environment> findByWorkspaceIdAndName(UUID workspaceId, String name);
}
