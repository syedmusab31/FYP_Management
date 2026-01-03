package com.university.fyp.dto;

import com.university.fyp.entity.RubricScore;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RubricScoreDTO {
    private Long id;
    private Long rubricGradeId;
    private Long criteriaId;
    private String criterionName;
    private BigDecimal maxScore;
    private BigDecimal score;
    private String feedback;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RubricScoreDTO fromScore(RubricScore score) {
        RubricScoreDTO dto = new RubricScoreDTO();
        dto.setId(score.getId());
        dto.setScore(score.getScore());
        dto.setFeedback(score.getFeedback());
        dto.setCreatedAt(score.getCreatedAt());
        dto.setUpdatedAt(score.getUpdatedAt());
        
        if (score.getRubricGrade() != null) {
            dto.setRubricGradeId(score.getRubricGrade().getId());
        }
        
        if (score.getCriteria() != null) {
            dto.setCriteriaId(score.getCriteria().getId());
            dto.setCriterionName(score.getCriteria().getCriterionName());
            dto.setMaxScore(score.getCriteria().getMaxScore());
        }
        
        return dto;
    }
}

