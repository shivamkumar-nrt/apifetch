package com.apiforge.backend.engine.controller;

import com.apiforge.backend.engine.dto.TestResultDto;
import com.apiforge.backend.engine.entity.TestResult;
import com.apiforge.backend.engine.repository.TestResultRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test-results")
@org.springframework.web.bind.annotation.CrossOrigin(origins = "*")
public class TestResultController {

    private final TestResultRepository testResultRepository;

    public TestResultController(TestResultRepository testResultRepository) {
        this.testResultRepository = testResultRepository;
    }

    @PostMapping
    public ResponseEntity<TestResultDto> saveTestResult(@RequestBody TestResultDto dto) {
        TestResult entity = new TestResult();
        entity.setRequestId(dto.getRequestId());
        entity.setExecutionId(dto.getExecutionId());
        entity.setAssertionType(dto.getAssertionType());
        entity.setPassed(dto.getPassed());
        entity.setExpected(dto.getExpected());
        entity.setActual(dto.getActual());
        
        TestResult saved = testResultRepository.save(entity);
        dto.setId(saved.getId());
        return ResponseEntity.ok(dto);
    }

    @org.springframework.web.bind.annotation.GetMapping
    public ResponseEntity<java.util.List<TestResultDto>> getAllTestResults() {
        java.util.List<TestResultDto> dtos = testResultRepository.findAll().stream().map(entity -> {
            TestResultDto dto = new TestResultDto();
            dto.setId(entity.getId());
            dto.setRequestId(entity.getRequestId());
            dto.setExecutionId(entity.getExecutionId());
            dto.setAssertionType(entity.getAssertionType());
            dto.setPassed(entity.getPassed());
            dto.setExpected(entity.getExpected());
            dto.setActual(entity.getActual());
            return dto;
        }).collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}
