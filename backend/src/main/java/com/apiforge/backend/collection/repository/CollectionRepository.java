package com.apiforge.backend.collection.repository;

import com.apiforge.backend.collection.entity.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, UUID> {
    List<Collection> findByWorkspaceId(UUID workspaceId);
    Optional<Collection> findByWorkspaceIdAndName(UUID workspaceId, String name);
}
