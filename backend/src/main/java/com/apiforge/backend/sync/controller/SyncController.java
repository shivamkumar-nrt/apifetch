package com.apiforge.backend.sync.controller;

import com.apiforge.backend.collection.dto.CollectionDto;
import com.apiforge.backend.collection.service.CollectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/sync")
public class SyncController {

    private final CollectionService collectionService;

    public SyncController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @PostMapping("/push")
    public ResponseEntity<String> pushCollections(@RequestBody List<CollectionDto> collections) {
        // Basic sync endpoint stub
        return ResponseEntity.ok("Synced " + collections.size() + " collections.");
    }

    @GetMapping("/pull")
    public ResponseEntity<List<CollectionDto>> pullCollections() {
        return ResponseEntity.ok(List.of()); // Requires getAllCollections implementation
    }
}
