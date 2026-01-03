package com.university.fyp.dto;

import com.university.fyp.entity.RubricCriteria;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class RubricCriteriaDTO {
    private Long id;
    private Long rubricId;
    private String criterionName;
    private String description;
    private BigDecimal maxScore;
    private Integer orderIndex;
    private Instant createdAt;
    private Instant updatedAt;

    public static RubricCriteriaDTO fromCriteria(RubricCriteria criteria) {
        RubricCriteriaDTO dto = new RubricCriteriaDTO();
        dto.setId(criteria.getId());
        dto.setCriterionName(criteria.getCriterionName());
        dto.setDescription(criteria.getDescription());
        dto.setMaxScore(criteria.getMaxScore());
        dto.setOrderIndex(criteria.getOrderIndex());
        dto.setCreatedAt(criteria.getCreatedAt());
        dto.setUpdatedAt(criteria.getUpdatedAt());
        
        if (criteria.getRubric() != null) {
            dto.setRubricId(criteria.getRubric().getId());
        }
        
        return dto;
    }
}

