package com.university.fyp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentReviewRequest {

    @NotBlank(message = "Review action is required. Must be 'APPROVE' or 'REVISION'")
    private String action; // "APPROVE" or "REVISION"

    private String comments; // Optional comments
}

