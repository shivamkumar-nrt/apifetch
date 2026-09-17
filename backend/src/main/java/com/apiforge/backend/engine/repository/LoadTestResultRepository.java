package com.apiforge.backend.engine.repository;

import com.apiforge.backend.engine.entity.LoadTestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LoadTestResultRepository extends JpaRepository<LoadTestResult, UUID> {
}
