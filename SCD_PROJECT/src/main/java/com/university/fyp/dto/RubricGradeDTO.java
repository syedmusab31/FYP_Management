package com.university.fyp.dto;

import com.university.fyp.entity.RubricGrade;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class RubricGradeDTO {
    private Long id;
    private Long rubricId;
    private String rubricTitle;
    private Long groupId;
    private String groupName;
    private Long documentId;
    private String documentTitle;
    private Long gradedById;
    private String gradedByName;
    private BigDecimal totalScore;
    private String overallFeedback;
    private Boolean isFinal;
    private Instant gradedAt;
    private Instant updatedAt;
    private List<RubricScoreDTO> scores;

    public static RubricGradeDTO fromRubricGrade(RubricGrade rubricGrade) {
        RubricGradeDTO dto = new RubricGradeDTO();
        dto.setId(rubricGrade.getId());
        dto.setTotalScore(rubricGrade.getTotalScore());
        dto.setOverallFeedback(rubricGrade.getOverallFeedback());
        dto.setIsFinal(rubricGrade.getIsFinal());
        dto.setGradedAt(rubricGrade.getGradedAt());
        dto.setUpdatedAt(rubricGrade.getUpdatedAt());
        
        if (rubricGrade.getRubric() != null) {
            dto.setRubricId(rubricGrade.getRubric().getId());
            dto.setRubricTitle(rubricGrade.getRubric().getTitle());
        }
        
        if (rubricGrade.getGroup() != null) {
            dto.setGroupId(rubricGrade.getGroup().getId());
            dto.setGroupName(rubricGrade.getGroup().getGroupName());
        }
        
        if (rubricGrade.getDocument() != null) {
            dto.setDocumentId(rubricGrade.getDocument().getId());
            dto.setDocumentTitle(rubricGrade.getDocument().getTitle());
        }
        
        if (rubricGrade.getGradedBy() != null) {
            dto.setGradedById(rubricGrade.getGradedBy().getId());
            dto.setGradedByName(rubricGrade.getGradedBy().getFullName());
        }
        
        if (rubricGrade.getScores() != null && !rubricGrade.getScores().isEmpty()) {
            dto.setScores(rubricGrade.getScores().stream()
                    .map(RubricScoreDTO::fromScore)
                    .collect(Collectors.toList()));
        }
        
        return dto;
    }
}

