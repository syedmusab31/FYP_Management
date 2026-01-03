package com.university.fyp.controller;

import com.university.fyp.dto.GradeRequest;
import com.university.fyp.dto.MessageResponse;
import com.university.fyp.entity.Grade;
import com.university.fyp.service.GradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class GradeController {

    private final GradeService gradeService;

    @PostMapping
    @PreAuthorize("hasAnyRole('COMMITTEE_MEMBER', 'FYP_COMMITTEE')")
    public ResponseEntity<?> assignGrade(@Valid @RequestBody GradeRequest gradeRequest) {
        try {
            Grade grade = gradeService.assignGrade(
                    gradeRequest.getGroupId(),
                    gradeRequest.getDocumentId(),
                    gradeRequest.getScore(),
                    gradeRequest.getFeedback(),
                    gradeRequest.getIsFinal());
            return ResponseEntity.ok(grade);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }

    @GetMapping("/{gradeId}")
    public ResponseEntity<?> getGrade(@PathVariable Long gradeId) {
        try {
            Grade grade = gradeService.getGradeById(gradeId);
            return ResponseEntity.ok(grade);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<?> getGradesByGroup(@PathVariable Long groupId) {
        try {
            return ResponseEntity.ok(gradeService.getGradesByGroup(groupId));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }

    @PutMapping("/{gradeId}/finalize")
    @PreAuthorize("hasRole('FYP_COMMITTEE')")
    public ResponseEntity<?> markGradeAsFinal(@PathVariable Long gradeId) {
        try {
            gradeService.markGradeAsFinal(gradeId);
            return ResponseEntity.ok(new MessageResponse("Grade marked as final successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }
}
