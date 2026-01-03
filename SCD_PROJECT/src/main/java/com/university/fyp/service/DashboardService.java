package com.university.fyp.service;

import com.university.fyp.dto.dashboard.*;
import com.university.fyp.dto.*;
import com.university.fyp.entity.*;
import com.university.fyp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.university.fyp.dto.UserDTO;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

        private final DocumentRepository documentRepository;
        private final GradeRepository gradeRepository;
        private final GroupRepository groupRepository;
        private final DeadlineRepository deadlineRepository;
        private final NotificationRepository notificationRepository;
        private final UserRepository userRepository;
        private final AuthService authService;

        public StudentDashboard getStudentDashboard() {
                User currentUser = authService.getCurrentUser();

                if (!currentUser.getRole().getName().equals("STUDENT")) {
                        throw new RuntimeException("Only students can access student dashboard");
                }

                if (currentUser.getGroup() == null) {
                        throw new RuntimeException("Student is not assigned to any group");
                }

                Long groupId = currentUser.getGroup().getId();

                StudentDashboard dashboard = new StudentDashboard();
                if (currentUser.getGroup() != null) {
                        Group group = currentUser.getGroup();
                        dashboard.setGroupInfo(GroupResponse.fromGroup(group));
                }

                // Populate members
                if (currentUser.getGroup().getMembers() != null) {
                        List<UserDTO> members = currentUser.getGroup().getMembers().stream()
                                        .map(UserDTO::fromUser)
                                        .collect(Collectors.toList());
                        dashboard.setGroupMembers(members);
                }

                // Get all documents for the group and convert to DTOs
                List<Document> documents = documentRepository.findByGroupId(groupId);
                List<DocumentDTO> documentDTOs = documents.stream()
                                .map(DocumentDTO::fromDocument)
                                .collect(Collectors.toList());
                dashboard.setDocuments(documentDTOs);

                // Get final grades only and convert to DTOs
                List<Grade> finalGrades = gradeRepository.findFinalGradesByGroupId(groupId);
                List<GradeDTO> finalGradeDTOs = finalGrades.stream()
                                .map(GradeDTO::fromGrade)
                                .collect(Collectors.toList());
                dashboard.setGrades(finalGradeDTOs);

                // Get upcoming deadlines and convert to DTOs
                List<DeadlineDTO> upcomingDeadlines = deadlineRepository
                                .findUpcomingDeadlines(java.time.Instant.now()).stream()
                                .map(DeadlineDTO::fromEntity)
                                .collect(Collectors.toList());
                dashboard.setUpcomingDeadlines(upcomingDeadlines);

                // Get recent notifications
                List<Notification> notifications = notificationRepository
                                .findByUserIdOrderByCreatedAtDesc(currentUser.getId());
                dashboard.setNotifications(notifications.stream().limit(10).toList());

                // Document statistics
                Map<String, Long> documentStats = new HashMap<>();
                documentStats.put("total", (long) documents.size());
                documentStats.put("draft",
                                documents.stream().filter(d -> d.getStatus() == Document.DocumentStatus.DRAFT).count());
                documentStats.put("submitted",
                                documents.stream().filter(d -> d.getStatus() == Document.DocumentStatus.SUBMITTED)
                                                .count());
                documentStats.put("approved",
                                documents.stream().filter(d -> d.getStatus() == Document.DocumentStatus.APPROVED)
                                                .count());
                documentStats.put("revision_requested",
                                documents.stream().filter(
                                                d -> d.getStatus() == Document.DocumentStatus.REVISION_REQUESTED)
                                                .count());
                dashboard.setDocumentStats(documentStats);

                return dashboard;
        }

        public SupervisorDashboard getSupervisorDashboard() {
                User currentUser = authService.getCurrentUser();

                if (!currentUser.getRole().getName().equals("SUPERVISOR")) {
                        throw new RuntimeException("Only supervisors can access supervisor dashboard");
                }

                SupervisorDashboard dashboard = new SupervisorDashboard();

                // Get supervised groups and convert to DTOs
                List<Group> supervisedGroups = groupRepository.findBySupervisorId(currentUser.getId());
                List<GroupResponse> supervisedGroupDTOs = supervisedGroups.stream()
                                .map(GroupResponse::fromGroup)
                                .collect(Collectors.toList());
                dashboard.setSupervisedGroups(supervisedGroupDTOs);

                // Get all documents from supervised groups and convert to DTOs
                List<Document> allDocuments = documentRepository.findBySupervisorId(currentUser.getId());
                List<DocumentDTO> allDocumentDTOs = allDocuments.stream()
                                .map(DocumentDTO::fromDocument)
                                .collect(Collectors.toList());
                dashboard.setAllDocuments(allDocumentDTOs);

                // Get documents pending review (submitted or under review - actually supervisor
                // cares about SUBMITTED)
                List<Document> pendingReview = allDocuments.stream()
                                .filter(d -> d.getStatus() == Document.DocumentStatus.SUBMITTED)
                                .toList();
                List<DocumentDTO> pendingReviewDTOs = pendingReview.stream()
                                .map(DocumentDTO::fromDocument)
                                .toList();
                dashboard.setPendingReviewDocuments(pendingReviewDTOs);

                // Statistics
                Map<String, Long> stats = new HashMap<>();
                stats.put("total_groups", (long) supervisedGroups.size());
                stats.put("total_documents", (long) allDocuments.size());
                stats.put("pending_review", (long) pendingReview.size());
                stats.put("approved",
                                allDocuments.stream().filter(d -> d.getStatus() == Document.DocumentStatus.APPROVED)
                                                .count());
                stats.put("approved_by_committee",
                                allDocuments.stream().filter(d -> d.getStatus() == Document.DocumentStatus.GRADED)
                                                .count());
                stats.put("revision_requested",
                                allDocuments.stream().filter(
                                                d -> d.getStatus() == Document.DocumentStatus.REVISION_REQUESTED)
                                                .count());
                dashboard.setStatistics(stats);

                return dashboard;
        }

        public CommitteeDashboard getCommitteeDashboard() {
                User currentUser = authService.getCurrentUser();

                if (!currentUser.getRole().getName().equals("COMMITTEE_MEMBER")) {
                        throw new RuntimeException("Only committee members can access committee dashboard");
                }

                CommitteeDashboard dashboard = new CommitteeDashboard();

                // Get all groups and convert to DTOs
                List<Group> allGroupsEntities = groupRepository.findAll();
                List<GroupResponse> allGroups = allGroupsEntities.stream()
                                .map(GroupResponse::fromGroup)
                                .collect(Collectors.toList());
                dashboard.setAllGroups(allGroups);

                // Get documents under review and convert to DTOs
                List<Document> underReview = documentRepository.findByStatus(Document.DocumentStatus.UNDER_REVIEW);
                List<DocumentDTO> underReviewDTOs = underReview.stream()
                                .map(DocumentDTO::fromDocument)
                                .collect(Collectors.toList());
                dashboard.setDocumentsUnderReview(underReviewDTOs);

                // Get approved documents and convert to DTOs
                List<Document> approved = documentRepository.findByStatus(Document.DocumentStatus.APPROVED);
                List<DocumentDTO> approvedDTOs = approved.stream()
                                .map(DocumentDTO::fromDocument)
                                .collect(Collectors.toList());
                dashboard.setApprovedDocuments(approvedDTOs);

                // Get all documents
                List<Document> allDocuments = documentRepository.findAll();

                // Statistics
                Map<String, Long> stats = new HashMap<>();
                stats.put("total_groups", (long) allGroupsEntities.size());
                stats.put("total_documents", (long) allDocuments.size());
                stats.put("under_review", (long) underReview.size());
                stats.put("approved", (long) approved.size());
                stats.put("graded", allDocuments.stream().filter(d -> d.getStatus() == Document.DocumentStatus.GRADED)
                                .count());
                dashboard.setStatistics(stats);

                return dashboard;
        }

        public FypCommitteeDashboard getFypCommitteeDashboard() {
                User currentUser = authService.getCurrentUser();

                if (!currentUser.getRole().getName().equals("FYP_COMMITTEE")) {
                        throw new RuntimeException("Only FYP Committee can access FYP Committee dashboard");
                }

                FypCommitteeDashboard dashboard = new FypCommitteeDashboard();

                // Get all groups and convert to DTOs
                List<GroupResponse> allGroups = groupRepository.findAll().stream()
                                .map(GroupResponse::fromGroup)
                                .collect(Collectors.toList());
                dashboard.setAllGroups(allGroups);

                // Get all documents and convert to DTOs
                List<Document> allDocumentsEntities = documentRepository.findAll();
                List<DocumentDTO> allDocuments = allDocumentsEntities.stream()
                                .map(DocumentDTO::fromDocument)
                                .collect(Collectors.toList());
                dashboard.setAllDocuments(allDocuments);

                // Get all grades and convert to DTOs
                List<Grade> allGrades = gradeRepository.findAll();
                List<com.university.fyp.dto.GradeDTO> allGradeDTOs = allGrades.stream()
                                .map(com.university.fyp.dto.GradeDTO::fromGrade)
                                .collect(Collectors.toList());
                dashboard.setAllGrades(allGradeDTOs);

                // Get final grades and convert to DTOs
                List<Grade> finalGrades = gradeRepository.findByIsFinal(true);
                List<com.university.fyp.dto.GradeDTO> finalGradeDTOs = finalGrades.stream()
                                .map(com.university.fyp.dto.GradeDTO::fromGrade)
                                .collect(Collectors.toList());
                dashboard.setFinalGrades(finalGradeDTOs);

                // Get all deadlines and convert to DTOs
                List<DeadlineDTO> allDeadlines = deadlineRepository.findAll().stream()
                                .map(DeadlineDTO::fromEntity)
                                .collect(Collectors.toList());
                dashboard.setAllDeadlines(allDeadlines);

                // Comprehensive statistics
                Map<String, Long> stats = new HashMap<>();
                stats.put("total_groups", (long) allGroups.size());
                stats.put("total_documents", (long) allDocuments.size());
                stats.put("total_grades", (long) allGrades.size());
                stats.put("final_grades", (long) finalGrades.size());
                stats.put("draft_documents",
                                allDocuments.stream().filter(d -> d.getStatus() == Document.DocumentStatus.DRAFT)
                                                .count());
                stats.put("submitted_documents",
                                allDocuments.stream().filter(d -> d.getStatus() == Document.DocumentStatus.SUBMITTED)
                                                .count());
                stats.put("under_review_documents",
                                allDocuments.stream().filter(d -> d.getStatus() == Document.DocumentStatus.UNDER_REVIEW)
                                                .count());
                stats.put("approved_documents",
                                allDocuments.stream().filter(d -> d.getStatus() == Document.DocumentStatus.APPROVED)
                                                .count());
                stats.put("graded_documents",
                                allDocuments.stream().filter(d -> d.getStatus() == Document.DocumentStatus.GRADED)
                                                .count());
                stats.put("late_submissions", allDocuments.stream().filter(DocumentDTO::getIsLate).count());
                dashboard.setStatistics(stats);

                // Populate User stats and lists
                List<UserDTO> students = userRepository.findByRoleName("STUDENT").stream()
                                .map(UserDTO::fromUser)
                                .collect(Collectors.toList());
                dashboard.setStudents(students);
                dashboard.setTotalStudents((long) students.size());

                List<UserDTO> supervisors = userRepository.findByRoleName("SUPERVISOR").stream()
                                .map(UserDTO::fromUser)
                                .collect(Collectors.toList());
                dashboard.setSupervisors(supervisors);
                dashboard.setTotalSupervisors((long) supervisors.size());

                List<UserDTO> committee = userRepository.findByRoleName("COMMITTEE_MEMBER").stream()
                                .map(UserDTO::fromUser)
                                .collect(Collectors.toList());
                dashboard.setCommitteeMembers(committee);
                dashboard.setTotalCommitteeMembers((long) committee.size());

                return dashboard;
        }
}
