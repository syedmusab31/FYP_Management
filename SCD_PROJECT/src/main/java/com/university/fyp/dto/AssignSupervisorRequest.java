package com.university.fyp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignSupervisorRequest {

    @NotNull(message = "Supervisor ID is required")
    private Long supervisorId;
}

