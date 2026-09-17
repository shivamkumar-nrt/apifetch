package com.apiforge.backend.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        // Hardcoded admin for SaaS demo purposes
        if ("masteradmin".equals(username)) {
            Map<String, Object> response = new HashMap<>();
            response.put("token", "fake-jwt-token-masteradmin");
            response.put("role", "MASTER_ADMIN");
            response.put("username", "masteradmin");
            return ResponseEntity.ok(response);
        } else if ("admin".equals(username)) {
            Map<String, Object> response = new HashMap<>();
            response.put("token", "fake-jwt-token-admin");
            response.put("role", "ADMIN");
            response.put("username", "admin");
            return ResponseEntity.ok(response);
        } else if ("dev".equals(username)) {
            Map<String, Object> response = new HashMap<>();
            response.put("token", "fake-jwt-token-dev");
            response.put("role", "DEVELOPER");
            response.put("username", "dev");
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User " + username + " registered successfully");
        response.put("role", "ADMIN");
        return ResponseEntity.ok(response);
    }
}
