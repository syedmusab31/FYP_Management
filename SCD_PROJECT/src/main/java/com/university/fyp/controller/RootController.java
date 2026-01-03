package com.university.fyp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public ResponseEntity<?> root() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", "FYP Management System");
        response.put("version", "1.0.0");
        response.put("status", "running");
        response.put("apiEndpoints", Map.of(
                "auth_login", "/api/auth/login (POST)",
                "auth_register", "/api/auth/register (POST)",
                "auth_current_user", "/api/auth/me (GET)",
                "documentation", "Check application.properties or README for more endpoints"
        ));
        return ResponseEntity.ok(response);
    }
}
