package com.apiforge.backend.environment.controller;

import com.apiforge.backend.environment.dto.EnvironmentDto;
import com.apiforge.backend.environment.service.EnvironmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/environments")
public class EnvironmentController {

    private final EnvironmentService environmentService;

    public EnvironmentController(EnvironmentService environmentService) {
        this.environmentService = environmentService;
    }

    @GetMapping
    public ResponseEntity<List<EnvironmentDto>> getEnvironments(@PathVariable UUID workspaceId) {
        return ResponseEntity.ok(environmentService.getEnvironmentsByWorkspace(workspaceId));
    }

    @PostMapping
    public ResponseEntity<EnvironmentDto> createEnvironment(
            @PathVariable UUID workspaceId,
            @RequestBody EnvironmentDto dto) {
        dto.setWorkspaceId(workspaceId);
        return ResponseEntity.ok(environmentService.createEnvironment(dto));
    }

    @DeleteMapping("/{environmentId}")
    public ResponseEntity<Void> deleteEnvironment(
            @PathVariable UUID workspaceId,
            @PathVariable UUID environmentId) {
        environmentService.deleteEnvironment(environmentId);
        return ResponseEntity.noContent().build();
    }
}
