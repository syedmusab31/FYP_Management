package com.university.fyp.dto.dashboard;

import com.university.fyp.entity.Document;
import com.university.fyp.entity.Group;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupervisorDashboard {
    private List<com.university.fyp.dto.GroupResponse> supervisedGroups;
    private List<com.university.fyp.dto.DocumentDTO> allDocuments;
    private List<com.university.fyp.dto.DocumentDTO> pendingReviewDocuments;
    private Map<String, Long> statistics;
}
