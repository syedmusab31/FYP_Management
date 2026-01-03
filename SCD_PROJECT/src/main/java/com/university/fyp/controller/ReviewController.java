package com.university.fyp.controller;

import com.university.fyp.dto.MessageResponse;
import com.university.fyp.dto.ReviewRequest;
import com.university.fyp.entity.Review;
import com.university.fyp.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'COMMITTEE_MEMBER')")
    public ResponseEntity<?> createReview(@Valid @RequestBody ReviewRequest reviewRequest) {
        try {
            Review review = reviewService.createReview(
                    reviewRequest.getDocumentId(),
                    reviewRequest.getComments(),
                    reviewRequest.getStatus());
            return ResponseEntity.ok(review);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<?> getReview(@PathVariable Long reviewId) {
        try {
            Review review = reviewService.getReviewById(reviewId);
            return ResponseEntity.ok(review);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }

    @GetMapping("/document/{documentId}")
    public ResponseEntity<?> getReviewsByDocument(@PathVariable Long documentId) {
        try {
            return ResponseEntity.ok(reviewService.getReviewsByDocument(documentId));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }

    @GetMapping("/reviewer/{reviewerId}")
    public ResponseEntity<?> getReviewsByReviewer(@PathVariable Long reviewerId) {
        try {
            return ResponseEntity.ok(reviewService.getReviewsByReviewer(reviewerId));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }
}
