package com.university.fyp.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(nullable = false, length = 100)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DocumentType type; // PROPOSAL, PROGRESS_REPORT, FINAL_REPORT, PRESENTATION

    @Column(nullable = false)
    private Integer version = 1;

    @Column(nullable = false, length = 500)
    private String filePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DocumentStatus status = DocumentStatus.DRAFT; // DRAFT, SUBMITTED, UNDER_REVIEW, APPROVED,
                                                          // REVISION_REQUESTED, GRADED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deadline_id")
    private Deadline deadline;

    private Instant submittedAt;

    @Column(nullable = false)
    private Boolean isLate = false;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    // Relationships
    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Review> reviews = new HashSet<>();

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Grade> grades = new HashSet<>();

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<VersionHistory> versionHistories = new HashSet<>();

    public enum DocumentType {
        PROPOSAL,
        PROGRESS_REPORT,
        FINAL_REPORT,
        PRESENTATION
    }

    public enum DocumentStatus {
        DRAFT,
        SUBMITTED,
        UNDER_REVIEW,
        APPROVED,
        REVISION_REQUESTED,
        GRADED
    }
}
