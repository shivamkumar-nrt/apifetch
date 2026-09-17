package com.apiforge.backend.monitoring.controller;

import com.apiforge.backend.monitoring.dto.MonitorJobDto;
import com.apiforge.backend.monitoring.dto.MonitorStatusDto;
import com.apiforge.backend.monitoring.entity.MonitorJob;
import com.apiforge.backend.monitoring.repository.MonitorJobRepository;
import com.apiforge.backend.monitoring.repository.MonitorStatusRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/monitor-jobs")
@CrossOrigin(origins = "*")
public class MonitorJobController {

    private final MonitorJobRepository jobRepository;
    private final MonitorStatusRepository statusRepository;

    public MonitorJobController(MonitorJobRepository jobRepository, MonitorStatusRepository statusRepository) {
        this.jobRepository = jobRepository;
        this.statusRepository = statusRepository;
    }

    @PostMapping
    public ResponseEntity<MonitorJobDto> createMonitorJob(@RequestBody MonitorJobDto dto) {
        MonitorJob job = new MonitorJob();
        job.setWorkspaceId(dto.getWorkspaceId());
        job.setName(dto.getName());
        job.setUrl(dto.getUrl());
        job.setMethod(dto.getMethod());
        job.setIntervalSeconds(dto.getIntervalSeconds());
        job.setActive(dto.isActive());
        
        MonitorJob saved = jobRepository.save(job);
        dto.setId(saved.getId());
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<List<MonitorJobDto>> getAllMonitorJobs() {
        List<MonitorJobDto> dtos = jobRepository.findAll().stream().map(entity -> {
            MonitorJobDto dto = new MonitorJobDto();
            dto.setId(entity.getId());
            dto.setWorkspaceId(entity.getWorkspaceId());
            dto.setName(entity.getName());
            dto.setUrl(entity.getUrl());
            dto.setMethod(entity.getMethod());
            dto.setIntervalSeconds(entity.getIntervalSeconds());
            dto.setActive(entity.getActive());
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/status")
    public ResponseEntity<List<MonitorStatusDto>> getAllMonitorStatus() {
        List<MonitorStatusDto> dtos = statusRepository.findAll().stream().map(entity -> {
            MonitorStatusDto dto = new MonitorStatusDto();
            dto.setId(entity.getId());
            dto.setJobId(entity.getJobId());
            dto.setUp(entity.getUp());
            dto.setStatusCode(entity.getStatusCode());
            dto.setResponseTimeMs(entity.getResponseTimeMs());
            dto.setCheckedAt(entity.getCheckedAt());
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}
