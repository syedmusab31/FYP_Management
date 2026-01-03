package com.university.fyp.controller;

import com.university.fyp.dto.*;
import com.university.fyp.service.RubricService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rubrics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class RubricController {

    private final RubricService rubricService;

    @PostMapping
    @PreAuthorize("hasRole('FYP_COMMITTEE')")
    public ResponseEntity<?> createRubric(@Valid @RequestBody RubricRequest request) {
        try {
            RubricDTO rubric = rubricService.createRubric(request);
            return ResponseEntity.ok(rubric);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllRubrics() {
        try {
            List<RubricDTO> rubrics = rubricService.getAllRubrics();
            return ResponseEntity.ok(rubrics);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActiveRubrics() {
        try {
            List<RubricDTO> rubrics = rubricService.getActiveRubrics();
            return ResponseEntity.ok(rubrics);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }

    @GetMapping("/type/{documentType}")
    public ResponseEntity<?> getRubricsByDocumentType(@PathVariable com.university.fyp.entity.Document.DocumentType documentType) {
        try {
            List<RubricDTO> rubrics = rubricService.getRubricsByDocumentType(documentType);
            return ResponseEntity.ok(rubrics);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRubricById(@PathVariable Long id) {
        try {
            RubricDTO rubric = rubricService.getRubricById(id);
            return ResponseEntity.ok(rubric);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FYP_COMMITTEE')")
    public ResponseEntity<?> updateRubric(@PathVariable Long id, @Valid @RequestBody RubricRequest request) {
        try {
            RubricDTO rubric = rubricService.updateRubric(id, request);
            return ResponseEntity.ok(rubric);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }

    @PostMapping("/grades")
    @PreAuthorize("hasAnyRole('COMMITTEE_MEMBER', 'FYP_COMMITTEE')")
    public ResponseEntity<?> assignRubricGrade(@Valid @RequestBody RubricGradeRequest request) {
        try {
            RubricGradeDTO grade = rubricService.assignRubricGrade(request);
            return ResponseEntity.ok(grade);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }

    @GetMapping("/grades/group/{groupId}")
    public ResponseEntity<?> getRubricGradesByGroup(@PathVariable Long groupId) {
        try {
            List<RubricGradeDTO> grades = rubricService.getRubricGradesByGroup(groupId);
            return ResponseEntity.ok(grades);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }

    @GetMapping("/grades/group/{groupId}/final")
    public ResponseEntity<?> getFinalRubricGradesByGroup(@PathVariable Long groupId) {
        try {
            List<RubricGradeDTO> grades = rubricService.getFinalRubricGradesByGroup(groupId);
            return ResponseEntity.ok(grades);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }

    @GetMapping("/grades/{id}")
    public ResponseEntity<?> getRubricGradeById(@PathVariable Long id) {
        try {
            RubricGradeDTO grade = rubricService.getRubricGradeById(id);
            return ResponseEntity.ok(grade);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }
}

