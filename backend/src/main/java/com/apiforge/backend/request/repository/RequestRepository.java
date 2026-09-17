package com.apiforge.backend.request.repository;

import com.apiforge.backend.request.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RequestRepository extends JpaRepository<Request, UUID> {
    List<Request> findByCollectionIdOrderByOrderIndexAsc(UUID collectionId);
    List<Request> findByFolderIdOrderByOrderIndexAsc(UUID folderId);
}
