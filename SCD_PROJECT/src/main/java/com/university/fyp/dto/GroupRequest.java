package com.university.fyp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GroupRequest {

    @NotBlank(message = "Group name is required")
    @Size(min = 3, max = 50, message = "Group name must be between 3 and 50 characters")
    private String groupName;

    @NotBlank(message = "Project title is required")
    @Size(min = 5, max = 255, message = "Project title must be between 5 and 255 characters")
    private String projectTitle;

    @Size(max = 2000, message = "Project description must not exceed 2000 characters")
    private String projectDescription;

    private Long supervisorId; // Optional - can assign supervisor later
}

