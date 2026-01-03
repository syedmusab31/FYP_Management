package com.university.fyp.dto;

import com.university.fyp.entity.Document;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadRequest {

    @NotNull(message = "Group ID is required")
    private Long groupId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Document type is required")
    private Document.DocumentType type;

    private String changeDescription;

    private Long deadlineId;
}
