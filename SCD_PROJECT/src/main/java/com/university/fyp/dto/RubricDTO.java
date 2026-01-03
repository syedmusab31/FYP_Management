package com.university.fyp.dto;

import com.university.fyp.entity.Rubric;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class RubricDTO {
    private Long id;
    private String title;
    private String description;
    private com.university.fyp.entity.Document.DocumentType documentType;
    private Boolean isActive;
    private Long createdById;
    private String createdByName;
    private Instant createdAt;
    private Instant updatedAt;
    private List<RubricCriteriaDTO> criteria;

    public static RubricDTO fromRubric(Rubric rubric) {
        RubricDTO dto = new RubricDTO();
        dto.setId(rubric.getId());
        dto.setTitle(rubric.getTitle());
        dto.setDescription(rubric.getDescription());
        dto.setDocumentType(rubric.getDocumentType());
        dto.setIsActive(rubric.getIsActive());
        
        if (rubric.getCreatedBy() != null) {
            dto.setCreatedById(rubric.getCreatedBy().getId());
            dto.setCreatedByName(rubric.getCreatedBy().getFullName());
        }
        
        dto.setCreatedAt(rubric.getCreatedAt());
        dto.setUpdatedAt(rubric.getUpdatedAt());
        
        if (rubric.getCriteria() != null && !rubric.getCriteria().isEmpty()) {
            dto.setCriteria(rubric.getCriteria().stream()
                    .map(RubricCriteriaDTO::fromCriteria)
                    .collect(Collectors.toList()));
        }
        
        return dto;
    }
}

