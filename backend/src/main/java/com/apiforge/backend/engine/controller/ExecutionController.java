package com.apiforge.backend.engine.controller;

import com.apiforge.backend.engine.core.RequestExecutor;
import com.apiforge.backend.engine.dto.ResponseDto;
import com.apiforge.backend.request.dto.RequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/execute")
public class ExecutionController {

    private final RequestExecutor requestExecutor;

    public ExecutionController(RequestExecutor requestExecutor) {
        this.requestExecutor = requestExecutor;
    }

    @PostMapping
    public ResponseEntity<ResponseDto> executeRequest(@RequestBody RequestDto request) {
        try {
            ResponseDto response = requestExecutor.execute(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // In a real scenario, map exceptions to appropriate HTTP status codes
            return ResponseEntity.internalServerError().build();
        }
    }
}
