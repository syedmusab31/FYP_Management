package com.university.fyp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RubricGradeRequest {
    @NotNull(message = "Rubric ID is required")
    private Long rubricId;

    @NotNull(message = "Group ID is required")
    private Long groupId;

    private Long documentId;

    private String overallFeedback;

    private Boolean isFinal = false;

    @NotNull(message = "Scores are required")
    private List<ScoreRequest> scores;
}

