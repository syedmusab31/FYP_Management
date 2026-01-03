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
public class CommitteeDashboard {
    private List<com.university.fyp.dto.GroupResponse> allGroups;
    private List<com.university.fyp.dto.DocumentDTO> documentsUnderReview;
    private List<com.university.fyp.dto.DocumentDTO> approvedDocuments;
    private Map<String, Long> statistics;
}
