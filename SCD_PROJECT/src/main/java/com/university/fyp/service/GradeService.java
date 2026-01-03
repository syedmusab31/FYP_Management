package com.university.fyp.service;

import com.university.fyp.entity.*;
import com.university.fyp.repository.DocumentRepository;
import com.university.fyp.repository.GradeRepository;
import com.university.fyp.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class GradeService {

    private final GradeRepository gradeRepository;
    private final GroupRepository groupRepository;
    private final DocumentRepository documentRepository;
    private final NotificationService notificationService;
    private final AuthService authService;

    @Transactional
    public Grade assignGrade(Long groupId, Long documentId, BigDecimal score,
            String feedback, Boolean isFinal) {
        User currentUser = authService.getCurrentUser();
        String roleName = currentUser.getRole().getName();

        // Only committee members and FYP committee can assign grades
        if (!roleName.equals("COMMITTEE_MEMBER") && !roleName.equals("FYP_COMMITTEE")) {
            throw new RuntimeException("Only committee members can assign grades");
        }

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        Document document = null;
        if (documentId != null) {
            document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new RuntimeException("Document not found"));

            // Ensure document belongs to the group
            if (!document.getGroup().getId().equals(groupId)) {
                throw new RuntimeException("Document does not belong to this group");
            }

            // Update document status to GRADED if final grade
            if (isFinal && document.getStatus() == Document.DocumentStatus.APPROVED) {
                document.setStatus(Document.DocumentStatus.GRADED);
                documentRepository.save(document);
            }
        }

        Grade grade = new Grade();
        grade.setGroup(group);
        grade.setDocument(document);
        grade.setScore(score);
        grade.setFeedback(feedback);
        grade.setIsFinal(isFinal);
        grade.setGradedBy(currentUser);

        Grade savedGrade = gradeRepository.save(grade);

        // Notify group members only if grade is final
        if (isFinal) {
            String message = String.format("Final grade assigned: %.2f for %s",
                    score, document != null ? document.getTitle() : "Project");

            group.getMembers().forEach(member -> notificationService.createNotification(
                    member,
                    message,
                    Notification.NotificationType.GRADE_RELEASED,
                    "Grade",
                    savedGrade.getId()));
        }

        return savedGrade;
    }

    public Grade getGradeById(Long gradeId) {
        User currentUser = authService.getCurrentUser();
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new RuntimeException("Grade not found"));

        // Students can only see final grades for their own group
        if (currentUser.getRole().getName().equals("STUDENT")) {
            if (!grade.getIsFinal()) {
                throw new RuntimeException("This grade is not yet finalized");
            }
            if (currentUser.getGroup() == null ||
                    !currentUser.getGroup().getId().equals(grade.getGroup().getId())) {
                throw new RuntimeException("You don't have permission to view this grade");
            }
        }

        return grade;
    }

    public java.util.List<Grade> getGradesByGroup(Long groupId) {
        User currentUser = authService.getCurrentUser();
        String roleName = currentUser.getRole().getName();

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        // Students can only see final grades for their own group
        if (roleName.equals("STUDENT")) {
            if (currentUser.getGroup() == null || !currentUser.getGroup().getId().equals(groupId)) {
                throw new RuntimeException("You don't have permission to view grades for this group");
            }
            return gradeRepository.findFinalGradesByGroupId(groupId);
        }

        // Supervisors can see all grades for their supervised groups
        if (roleName.equals("SUPERVISOR")) {
            if (group.getSupervisor() == null || !group.getSupervisor().getId().equals(currentUser.getId())) {
                throw new RuntimeException("You don't have permission to view grades for this group");
            }
        }

        // Committee members and FYP committee can see all grades
        return gradeRepository.findByGroupId(groupId);
    }

    @Transactional
    public void markGradeAsFinal(Long gradeId) {
        User currentUser = authService.getCurrentUser();
        String roleName = currentUser.getRole().getName();

        // Only FYP committee can mark grades as final
        if (!roleName.equals("FYP_COMMITTEE")) {
            throw new RuntimeException("Only FYP Committee can mark grades as final");
        }

        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new RuntimeException("Grade not found"));

        grade.setIsFinal(true);
        Grade savedGrade = gradeRepository.save(grade);

        // Notify group members
        String message = String.format("Final grade announced: %.2f", grade.getScore());
        grade.getGroup().getMembers().forEach(member -> notificationService.createNotification(
                member,
                message,
                Notification.NotificationType.GRADE_RELEASED,
                "Grade",
                savedGrade.getId()));
    }
}
