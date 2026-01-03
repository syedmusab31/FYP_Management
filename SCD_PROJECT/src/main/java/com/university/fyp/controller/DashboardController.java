package com.university.fyp.controller;

import com.university.fyp.dto.MessageResponse;
import com.university.fyp.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/student")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getStudentDashboard() {
        try {
            return ResponseEntity.ok(dashboardService.getStudentDashboard());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }

    @GetMapping("/supervisor")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<?> getSupervisorDashboard() {
        try {
            return ResponseEntity.ok(dashboardService.getSupervisorDashboard());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }

    @GetMapping("/committee")
    @PreAuthorize("hasRole('COMMITTEE_MEMBER')")
    public ResponseEntity<?> getCommitteeDashboard() {
        try {
            return ResponseEntity.ok(dashboardService.getCommitteeDashboard());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }

    @GetMapping("/fyp-committee")
    @PreAuthorize("hasRole('FYP_COMMITTEE')")
    public ResponseEntity<?> getFypCommitteeDashboard() {
        try {
            return ResponseEntity.ok(dashboardService.getFypCommitteeDashboard());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }
}
