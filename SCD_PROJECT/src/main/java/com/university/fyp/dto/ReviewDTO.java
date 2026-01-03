package com.university.fyp.dto;

import com.university.fyp.entity.Review;
import lombok.Data;

import java.time.Instant;

@Data
public class ReviewDTO {
    private Long id;
    private Long documentId;
    private String documentTitle;
    private Long reviewerId;
    private String reviewerName;
    private String reviewerEmail;
    private String comments;
    private Review.ReviewStatus status;
    private Instant reviewedAt;

    public static ReviewDTO fromReview(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        
        // Document information
        if (review.getDocument() != null) {
            dto.setDocumentId(review.getDocument().getId());
            dto.setDocumentTitle(review.getDocument().getTitle());
        }
        
        // Reviewer information
        if (review.getReviewer() != null) {
            dto.setReviewerId(review.getReviewer().getId());
            dto.setReviewerName(review.getReviewer().getFullName());
            dto.setReviewerEmail(review.getReviewer().getEmail());
        }
        
        dto.setComments(review.getComments());
        dto.setStatus(review.getStatus());
        dto.setReviewedAt(review.getReviewedAt());
        
        return dto;
    }
}

