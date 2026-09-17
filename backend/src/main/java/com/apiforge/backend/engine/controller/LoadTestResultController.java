package com.apiforge.backend.engine.controller;

import com.apiforge.backend.engine.dto.LoadTestResultDto;
import com.apiforge.backend.engine.entity.LoadTestResult;
import com.apiforge.backend.engine.repository.LoadTestResultRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/load-test-results")
@org.springframework.web.bind.annotation.CrossOrigin(origins = "*")
public class LoadTestResultController {

    private final LoadTestResultRepository repository;

    public LoadTestResultController(LoadTestResultRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<LoadTestResultDto> saveLoadTestResult(@RequestBody LoadTestResultDto dto) {
        LoadTestResult entity = new LoadTestResult();
        entity.setWorkspaceId(dto.getWorkspaceId());
        entity.setUrl(dto.getUrl());
        entity.setMethod(dto.getMethod());
        entity.setConcurrentUsers(dto.getConcurrentUsers());
        entity.setTotalRequests(dto.getTotalRequests());
        entity.setSuccessCount(dto.getSuccessCount());
        entity.setFailureCount(dto.getFailureCount());
        entity.setMinLatencyMs(dto.getMinLatencyMs());
        entity.setMaxLatencyMs(dto.getMaxLatencyMs());
        entity.setAvgLatencyMs(dto.getAvgLatencyMs());
        entity.setP50Ms(dto.getP50Ms());
        entity.setP90Ms(dto.getP90Ms());
        entity.setP99Ms(dto.getP99Ms());
        entity.setTps(dto.getTps());

        LoadTestResult saved = repository.save(entity);
        dto.setId(saved.getId());
        return ResponseEntity.ok(dto);
    }

    @org.springframework.web.bind.annotation.GetMapping
    public ResponseEntity<java.util.List<LoadTestResultDto>> getAllLoadTestResults() {
        java.util.List<LoadTestResultDto> dtos = repository.findAll().stream().map(entity -> {
            LoadTestResultDto dto = new LoadTestResultDto();
            dto.setId(entity.getId());
            dto.setWorkspaceId(entity.getWorkspaceId());
            dto.setUrl(entity.getUrl());
            dto.setMethod(entity.getMethod());
            dto.setConcurrentUsers(entity.getConcurrentUsers());
            dto.setTotalRequests(entity.getTotalRequests());
            dto.setSuccessCount(entity.getSuccessCount());
            dto.setFailureCount(entity.getFailureCount());
            dto.setMinLatencyMs(entity.getMinLatencyMs());
            dto.setMaxLatencyMs(entity.getMaxLatencyMs());
            dto.setAvgLatencyMs(entity.getAvgLatencyMs());
            dto.setP50Ms(entity.getP50Ms());
            dto.setP90Ms(entity.getP90Ms());
            dto.setP99Ms(entity.getP99Ms());
            dto.setTps(entity.getTps());
            return dto;
        }).collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}
