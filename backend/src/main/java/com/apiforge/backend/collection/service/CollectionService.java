package com.apiforge.backend.collection.service;

import com.apiforge.backend.collection.dto.CollectionDto;
import com.apiforge.backend.collection.entity.Collection;
import com.apiforge.backend.collection.repository.CollectionRepository;
import com.apiforge.backend.workspace.entity.Workspace;
import com.apiforge.backend.workspace.repository.WorkspaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final WorkspaceRepository workspaceRepository;

    public CollectionService(CollectionRepository collectionRepository, WorkspaceRepository workspaceRepository) {
        this.collectionRepository = collectionRepository;
        this.workspaceRepository = workspaceRepository;
    }

    @Transactional(readOnly = true)
    public List<CollectionDto> getCollectionsByWorkspace(UUID workspaceId) {
        return collectionRepository.findByWorkspaceId(workspaceId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CollectionDto createCollection(CollectionDto dto) {
        Workspace workspace = workspaceRepository.findById(dto.getWorkspaceId())
                .orElseThrow(() -> new RuntimeException("Workspace not found"));
                
        Collection collection = new Collection();
        collection.setWorkspace(workspace);
        collection.setName(dto.getName());
        collection.setDescription(dto.getDescription());
        
        Collection saved = collectionRepository.save(collection);
        return mapToDto(saved);
    }
    
    @Transactional
    public CollectionDto updateCollection(UUID id, CollectionDto dto) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Collection not found"));
                
        collection.setName(dto.getName());
        collection.setDescription(dto.getDescription());
        
        return mapToDto(collectionRepository.save(collection));
    }
    
    @Transactional
    public void deleteCollection(UUID id) {
        collectionRepository.deleteById(id);
    }

    private CollectionDto mapToDto(Collection collection) {
        CollectionDto dto = new CollectionDto();
        dto.setId(collection.getId());
        dto.setWorkspaceId(collection.getWorkspace().getId());
        dto.setName(collection.getName());
        dto.setDescription(collection.getDescription());
        return dto;
    }
}
