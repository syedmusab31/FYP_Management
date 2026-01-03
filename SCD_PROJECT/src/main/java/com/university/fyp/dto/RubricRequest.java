package com.university.fyp.dto;

import com.university.fyp.entity.Rubric;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RubricRequest {
    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Document type is required")
    private com.university.fyp.entity.Document.DocumentType documentType;

    private Boolean isActive = true;

    @NotNull(message = "Criteria list is required")
    private List<CriteriaRequest> criteria;
}

