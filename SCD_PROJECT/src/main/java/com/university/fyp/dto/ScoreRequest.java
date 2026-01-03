package com.university.fyp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreRequest {
    @NotNull(message = "Criteria ID is required")
    private Long criteriaId;

    @NotNull(message = "Score is required")
    @PositiveOrZero(message = "Score must be positive or zero")
    private BigDecimal score;

    private String feedback;
}

