package com.university.fyp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "rubric_grades")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class RubricGrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rubric_id", nullable = false)
    private Rubric rubric;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "graded_by", nullable = false)
    private User gradedBy;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal totalScore; // Sum of all criteria scores

    @Column(columnDefinition = "TEXT")
    private String overallFeedback;

    @Column(nullable = false)
    private Boolean isFinal = false;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant gradedAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    // Relationships
    @OneToMany(mappedBy = "rubricGrade", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RubricScore> scores = new HashSet<>();
}

