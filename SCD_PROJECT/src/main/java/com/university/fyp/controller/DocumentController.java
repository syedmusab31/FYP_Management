package com.university.fyp.controller;

import com.university.fyp.dto.DocumentDTO;
import com.university.fyp.dto.DocumentReviewRequest;
import com.university.fyp.dto.MessageResponse;
import com.university.fyp.dto.ReviewDTO;
import com.university.fyp.entity.Document;
import com.university.fyp.repository.GradeRepository;
import com.university.fyp.service.DocumentService;
import com.university.fyp.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class DocumentController {

    private final DocumentService documentService;
    private final ReviewService reviewService;
    private final GradeRepository gradeRepository;

    @PostMapping("/upload")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("groupId") Long groupId,
            @RequestParam("title") String title,
            @RequestParam("type") Document.DocumentType type,
            @RequestParam(value = "changeDescription", required = false) String changeDescription,
            @RequestParam(value = "deadlineId", required = false) Long deadlineId) {
        try {
            Document document = documentService.uploadDocument(
                    groupId, title, type, file, changeDescription, deadlineId);
            DocumentDTO documentDTO = DocumentDTO.fromDocument(document);
            return ResponseEntity.ok(documentDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }

    @PutMapping("/{documentId}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> submitDocument(@PathVariable Long documentId) {
        try {
            Document document = documentService.submitDocument(documentId);
            DocumentDTO documentDTO = DocumentDTO.fromDocument(document);
            return ResponseEntity.ok(documentDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }

    @PutMapping("/{documentId}/review")
    //also allow COMMITTEE_MEMBER to review for cases of REVISION_REQUESTED
    @PreAuthorize("hasRole('SUPERVISOR') or hasRole('COMMITTEE_MEMBER')")
    public ResponseEntity<?> reviewDocument(
            @PathVariable Long documentId,
            @Valid @RequestBody DocumentReviewRequest reviewRequest) {
        try {
            Document document = documentService.reviewDocument(
                    documentId,
                    reviewRequest.getAction(),
                    reviewRequest.getComments());
            DocumentDTO documentDTO = DocumentDTO.fromDocument(document);
            return ResponseEntity.ok(documentDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }

    @PutMapping("/{documentId}/status")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'COMMITTEE_MEMBER', 'FYP_COMMITTEE')")
    public ResponseEntity<?> updateDocumentStatus(
            @PathVariable Long documentId,
            @RequestParam Document.DocumentStatus status) {
        try {
            Document document = documentService.updateDocumentStatus(documentId, status);
            DocumentDTO documentDTO = DocumentDTO.fromDocument(document);
            return ResponseEntity.ok(documentDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<?> getDocument(@PathVariable Long documentId) {
        try {
            DocumentDTO documentDTO = documentService.getDocumentDTOById(documentId);
            return ResponseEntity.ok(documentDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<?> getDocumentsByGroup(@PathVariable Long groupId) {
        try {
            return ResponseEntity.ok(documentService.getDocumentDTOsByGroup(groupId));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }

    @GetMapping("/supervisor/{supervisorId}")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'COMMITTEE_MEMBER', 'FYP_COMMITTEE')")
    public ResponseEntity<?> getDocumentsBySupervisor(@PathVariable Long supervisorId) {
        try {
            return ResponseEntity.ok(documentService.getDocumentDTOsBySupervisor(supervisorId));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('COMMITTEE_MEMBER', 'FYP_COMMITTEE')")
    public ResponseEntity<?> getDocumentsByStatus(@PathVariable Document.DocumentStatus status) {
        try {
            return ResponseEntity.ok(documentService.getAllDocumentDTOsByStatus(status));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }

    @GetMapping("/committee/gradable")
    @PreAuthorize("hasAnyRole('COMMITTEE_MEMBER', 'FYP_COMMITTEE')")
    public ResponseEntity<?> getGradableDocuments() {
        try {
            // Fetch both APPROVED and REVISION_REQUESTED documents for committee grading
            List<DocumentDTO> approved = documentService.getAllDocumentDTOsByStatus(Document.DocumentStatus.APPROVED);
            List<DocumentDTO> revisionRequested = documentService
                    .getAllDocumentDTOsByStatus(Document.DocumentStatus.REVISION_REQUESTED);

            // Combine both lists
            approved.addAll(revisionRequested);

            // Filter out documents that already have grades (to prevent duplicate grading)
            List<DocumentDTO> ungradedDocs = approved.stream()
                    .filter(doc -> {
                        // Check if this document has any grades
                        List<?> grades = gradeRepository.findByDocumentId(doc.getId());
                        return grades.isEmpty();
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ungradedDocs);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }

    @GetMapping("/{documentId}/versions")
    public ResponseEntity<?> getDocumentVersionHistory(@PathVariable Long documentId) {
        try {
            return ResponseEntity.ok(documentService.getDocumentVersionHistory(documentId));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }

    @GetMapping("/{documentId}/reviews")
    public ResponseEntity<?> getDocumentReviews(@PathVariable Long documentId) {
        try {
            return ResponseEntity.ok(reviewService.getReviewsByDocument(documentId).stream()
                    .map(ReviewDTO::fromReview)
                    .toList());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }
}
