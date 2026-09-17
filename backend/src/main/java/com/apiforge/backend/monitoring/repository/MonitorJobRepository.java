package com.apiforge.backend.monitoring.repository;

import com.apiforge.backend.monitoring.entity.MonitorJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MonitorJobRepository extends JpaRepository<MonitorJob, UUID> {
    List<MonitorJob> findByIsActiveTrue();
}
