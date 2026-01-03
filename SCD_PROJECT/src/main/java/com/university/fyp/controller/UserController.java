package com.university.fyp.controller;

import com.university.fyp.dto.MessageResponse;
import com.university.fyp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    private final UserService userService;

    @GetMapping("/supervisors")
    @PreAuthorize("hasRole('FYP_COMMITTEE')")
    public ResponseEntity<?> getSupervisors() {
        try {
            return ResponseEntity.ok(userService.getSupervisors());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }

    @GetMapping("/students/available")
    @PreAuthorize("hasRole('FYP_COMMITTEE')")
    public ResponseEntity<?> getAvailableStudents() {
        try {
            return ResponseEntity.ok(userService.getAvailableStudents());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }
}
