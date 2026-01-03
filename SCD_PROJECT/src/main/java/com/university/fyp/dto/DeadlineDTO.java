package com.university.fyp.dto;

import com.university.fyp.entity.Deadline;
import com.university.fyp.entity.Document;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeadlineDTO {
    private Long id;
    private String title;
    private String description;
    private Document.DocumentType documentType;
    private Instant dueDate;
    private Boolean isActive;

    public static DeadlineDTO fromEntity(Deadline deadline) {
        DeadlineDTO dto = new DeadlineDTO();
        dto.setId(deadline.getId());
        dto.setTitle(deadline.getTitle());
        dto.setDescription(deadline.getDescription());
        dto.setDocumentType(deadline.getDocumentType());
        dto.setDueDate(deadline.getDueDate());
        dto.setIsActive(deadline.getIsActive());
        return dto;
    }
}
