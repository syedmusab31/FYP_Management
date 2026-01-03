package com.university.fyp.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradeRequest {

    @NotNull(message = "Group ID is required")
    private Long groupId;

    private Long documentId; // Optional, can be null for overall project grade

    @NotNull(message = "Score is required")
    @DecimalMin(value = "0.0", message = "Score must be at least 0")
    @DecimalMax(value = "100.0", message = "Score must not exceed 100")
    private BigDecimal score;

    private String feedback;

    @NotNull(message = "Final flag is required")
    private Boolean isFinal;
}
