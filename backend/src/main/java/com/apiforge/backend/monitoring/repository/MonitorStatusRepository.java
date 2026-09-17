package com.apiforge.backend.monitoring.repository;

import com.apiforge.backend.monitoring.entity.MonitorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MonitorStatusRepository extends JpaRepository<MonitorStatus, UUID> {
}
