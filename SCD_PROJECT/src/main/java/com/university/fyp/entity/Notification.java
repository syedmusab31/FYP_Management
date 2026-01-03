package com.university.fyp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 255)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private NotificationType type; // DOCUMENT_APPROVED, REVISION_REQUESTED, GRADE_ASSIGNED, DEADLINE_REMINDER,
                                   // GENERAL

    @Column(nullable = false)
    private Boolean isRead = false;

    @Column(length = 500)
    private String relatedEntityType; // e.g., "Document", "Grade"

    private Long relatedEntityId;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public enum NotificationType {
        // Student notifications
        GRADE_RELEASED,
        DEADLINE_CREATED,
        DOCUMENT_APPROVED,
        REVISION_REQUESTED,
        
        // Supervisor notifications
        DOCUMENT_UPLOADED,
        DOCUMENT_RESUBMITTED,
        COMMITTEE_REVISION_REQUESTED,
        
        // Committee notifications
        GRADES_RELEASED,
        DOCUMENT_RESUBMITTED_FOR_REVIEW,
        
        // FYP Committee notifications
        GRADES_COMPLETED,
        
        // General
        GENERAL
    }
}
