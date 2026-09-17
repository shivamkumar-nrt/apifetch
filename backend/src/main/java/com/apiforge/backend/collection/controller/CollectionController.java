package com.apiforge.backend.collection.controller;

import com.apiforge.backend.collection.dto.CollectionDto;
import com.apiforge.backend.collection.service.CollectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/collections")
public class CollectionController {

    private final CollectionService collectionService;

    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @GetMapping
    public ResponseEntity<List<CollectionDto>> getCollections(@PathVariable UUID workspaceId) {
        return ResponseEntity.ok(collectionService.getCollectionsByWorkspace(workspaceId));
    }

    @PostMapping
    public ResponseEntity<CollectionDto> createCollection(
            @PathVariable UUID workspaceId,
            @RequestBody CollectionDto dto) {
        dto.setWorkspaceId(workspaceId);
        return ResponseEntity.ok(collectionService.createCollection(dto));
    }

    @PutMapping("/{collectionId}")
    public ResponseEntity<CollectionDto> updateCollection(
            @PathVariable UUID workspaceId,
            @PathVariable UUID collectionId,
            @RequestBody CollectionDto dto) {
        return ResponseEntity.ok(collectionService.updateCollection(collectionId, dto));
    }

    @DeleteMapping("/{collectionId}")
    public ResponseEntity<Void> deleteCollection(
            @PathVariable UUID workspaceId,
            @PathVariable UUID collectionId) {
        collectionService.deleteCollection(collectionId);
        return ResponseEntity.noContent().build();
    }
}
