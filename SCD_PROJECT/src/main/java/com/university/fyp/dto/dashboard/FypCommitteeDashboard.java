package com.university.fyp.dto.dashboard;

import com.university.fyp.dto.GradeDTO;
import com.university.fyp.dto.GroupResponse;
import com.university.fyp.dto.DocumentDTO;
import com.university.fyp.dto.UserDTO;
import com.university.fyp.dto.DeadlineDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FypCommitteeDashboard {
    private List<GroupResponse> allGroups;
    private List<DocumentDTO> allDocuments;
    private List<GradeDTO> allGrades;
    private List<GradeDTO> finalGrades;
    private List<DeadlineDTO> allDeadlines;
    private Map<String, Long> statistics;

    // New statistics
    private Long totalStudents;
    private Long totalSupervisors;
    private Long totalCommitteeMembers;

    // Detailed lists
    private List<UserDTO> students;
    private List<UserDTO> supervisors;
    private List<UserDTO> committeeMembers;
}
