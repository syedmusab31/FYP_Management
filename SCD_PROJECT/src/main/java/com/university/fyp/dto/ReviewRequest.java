package com.university.fyp.dto;

import com.university.fyp.entity.Review;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {

    @NotNull(message = "Document ID is required")
    private Long documentId;

    @NotBlank(message = "Comments are required")
    private String comments;

    @NotNull(message = "Review status is required")
    private Review.ReviewStatus status;
}
