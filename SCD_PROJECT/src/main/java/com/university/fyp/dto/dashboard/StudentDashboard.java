package com.university.fyp.dto.dashboard;

import com.university.fyp.entity.Deadline;
import com.university.fyp.entity.Document;
import com.university.fyp.entity.Grade;
import com.university.fyp.entity.Group;
import com.university.fyp.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentDashboard {
    private com.university.fyp.dto.GroupResponse groupInfo;
    private List<com.university.fyp.dto.DocumentDTO> documents;
    private List<com.university.fyp.dto.GradeDTO> grades;
    private List<com.university.fyp.dto.DeadlineDTO> upcomingDeadlines;
    private List<Notification> notifications; // Notification is simple enough or needs DTO? Leaving for now as it
                                              // usually has no circular deps if carefully matched, but safer to use
                                              // DTO. Let's stick to strict DTOs.
    // Actually Notification checks User which checks... circular.
    // Ideally NotificationDTO.
    private Map<String, Long> documentStats;
    private List<com.university.fyp.dto.UserDTO> groupMembers;
}
