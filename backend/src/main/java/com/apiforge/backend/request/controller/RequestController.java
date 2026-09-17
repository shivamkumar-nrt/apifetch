package com.apiforge.backend.request.controller;

import com.apiforge.backend.request.dto.RequestDto;
import com.apiforge.backend.request.service.RequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/collections/{collectionId}/requests")
public class RequestController {

    private final RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @GetMapping
    public ResponseEntity<List<RequestDto>> getRequests(@PathVariable UUID collectionId) {
        return ResponseEntity.ok(requestService.getRequestsByCollection(collectionId));
    }

    @PostMapping
    public ResponseEntity<RequestDto> createRequest(
            @PathVariable UUID collectionId,
            @RequestBody RequestDto dto) {
        dto.setCollectionId(collectionId);
        return ResponseEntity.ok(requestService.createRequest(dto));
    }

    @PutMapping("/{requestId}")
    public ResponseEntity<RequestDto> updateRequest(
            @PathVariable UUID collectionId,
            @PathVariable UUID requestId,
            @RequestBody RequestDto dto) {
        return ResponseEntity.ok(requestService.updateRequest(requestId, dto));
    }

    @DeleteMapping("/{requestId}")
    public ResponseEntity<Void> deleteRequest(
            @PathVariable UUID collectionId,
            @PathVariable UUID requestId) {
        requestService.deleteRequest(requestId);
        return ResponseEntity.noContent().build();
    }
}
