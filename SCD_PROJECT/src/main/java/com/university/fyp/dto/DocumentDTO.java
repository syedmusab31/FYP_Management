package com.university.fyp.dto;

import com.university.fyp.entity.Document;
import lombok.Data;

import java.time.Instant;

@Data
public class DocumentDTO {
    private Long id;
    private Long groupId;
    private String groupName;
    private String projectTitle;
    private String supervisorName;
    private String title;
    private Document.DocumentType type;
    private Integer version;
    private String filePath;
    private Document.DocumentStatus status;
    private Long uploadedById;
    private String uploadedByName;
    private String uploadedByEmail;
    private Long deadlineId;
    private String deadlineTitle;
    private Instant deadlineDate;
    private Instant submittedAt;
    private Boolean isLate;
    private Instant createdAt;

    public static DocumentDTO fromDocument(Document document) {
        DocumentDTO dto = new DocumentDTO();
        dto.setId(document.getId());

        // Group information
        if (document.getGroup() != null) {
            dto.setGroupId(document.getGroup().getId());
            dto.setGroupName(document.getGroup().getGroupName());
            dto.setProjectTitle(document.getGroup().getProjectTitle());

            // Supervisor information
            if (document.getGroup().getSupervisor() != null) {
                dto.setSupervisorName(document.getGroup().getSupervisor().getFullName());
            }
        }

        dto.setTitle(document.getTitle());
        dto.setType(document.getType());
        dto.setVersion(document.getVersion());
        dto.setFilePath(document.getFilePath());
        dto.setStatus(document.getStatus());

        // Uploaded by information
        if (document.getUploadedBy() != null) {
            dto.setUploadedById(document.getUploadedBy().getId());
            dto.setUploadedByName(document.getUploadedBy().getFullName());
            dto.setUploadedByEmail(document.getUploadedBy().getEmail());
        }

        // Deadline information
        if (document.getDeadline() != null) {
            dto.setDeadlineId(document.getDeadline().getId());
            dto.setDeadlineTitle(document.getDeadline().getTitle());
            dto.setDeadlineDate(document.getDeadline().getDueDate());
        }

        dto.setSubmittedAt(document.getSubmittedAt());
        dto.setIsLate(document.getIsLate());
        dto.setCreatedAt(document.getCreatedAt());

        return dto;
    }
}
