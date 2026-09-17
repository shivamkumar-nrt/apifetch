package com.apiforge.backend.monitoring.service;

import com.apiforge.backend.monitoring.entity.MonitorJob;
import com.apiforge.backend.monitoring.entity.MonitorStatus;
import com.apiforge.backend.monitoring.repository.MonitorJobRepository;
import com.apiforge.backend.monitoring.repository.MonitorStatusRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class MonitoringService {

    private final MonitorJobRepository jobRepository;
    private final MonitorStatusRepository statusRepository;
    private final HttpClient httpClient;

    public MonitoringService(MonitorJobRepository jobRepository, MonitorStatusRepository statusRepository) {
        this.jobRepository = jobRepository;
        this.statusRepository = statusRepository;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    // Run every 60 seconds (in a real app, this would be a dynamic Quartz scheduler)
    @Scheduled(fixedRate = 60000)
    public void executeMonitorJobs() {
        List<MonitorJob> activeJobs = jobRepository.findByIsActiveTrue();
        
        for (MonitorJob job : activeJobs) {
            // Simplified check: we assume the fixedRate covers it, or we check last run time.
            checkEndpoint(job);
        }
    }

    private void checkEndpoint(MonitorJob job) {
        long startTime = System.currentTimeMillis();
        MonitorStatus status = new MonitorStatus();
        status.setJobId(job.getId());
        status.setCheckedAt(ZonedDateTime.now());
        
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(job.getUrl()))
                    .method(job.getMethod().toUpperCase(), HttpRequest.BodyPublishers.noBody())
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            
            long endTime = System.currentTimeMillis();
            status.setStatusCode(response.statusCode());
            status.setResponseTimeMs(endTime - startTime);
            status.setUp(response.statusCode() >= 200 && response.statusCode() < 300);
            
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            status.setStatusCode(0);
            status.setResponseTimeMs(endTime - startTime);
            status.setUp(false);
        }
        
        statusRepository.save(status);
    }
}
