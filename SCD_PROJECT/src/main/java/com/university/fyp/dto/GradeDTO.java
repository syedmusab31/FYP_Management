package com.university.fyp.dto;

import com.university.fyp.entity.Grade;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class GradeDTO {
    private Long id;
    private Long groupId;
    private String groupName;
    private Long documentId;
    private String documentTitle;
    private BigDecimal score;
    private String feedback;
    private Long gradedById;
    private String gradedByName;
    private Boolean isFinal;
    private Instant gradedAt;

    public static GradeDTO fromGrade(Grade grade) {
        GradeDTO dto = new GradeDTO();
        dto.setId(grade.getId());
        dto.setScore(grade.getScore());
        dto.setFeedback(grade.getFeedback());
        dto.setIsFinal(grade.getIsFinal());
        dto.setGradedAt(grade.getGradedAt());

        if (grade.getGroup() != null) {
            dto.setGroupId(grade.getGroup().getId());
            dto.setGroupName(grade.getGroup().getGroupName());
        }

        if (grade.getDocument() != null) {
            dto.setDocumentId(grade.getDocument().getId());
            dto.setDocumentTitle(grade.getDocument().getTitle());
        }

        if (grade.getGradedBy() != null) {
            dto.setGradedById(grade.getGradedBy().getId());
            dto.setGradedByName(grade.getGradedBy().getFullName());
        }

        return dto;
    }
}
