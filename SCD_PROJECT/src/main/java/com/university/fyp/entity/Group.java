package com.university.fyp.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "fyp_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners({ AuditingEntityListener.class, GroupEntityListener.class })
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = { "supervisor", "members", "documents", "grades", "rubricGrades" })
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String groupName;

    @Column(nullable = false, length = 255)
    private String projectTitle;

    @Column(columnDefinition = "TEXT")
    private String projectDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_id")
    @JsonIgnoreProperties({ "group", "uploadedDocuments", "reviews", "grades", "notifications" })
    private User supervisor;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    // Member IDs stored as JSON array in database
    @Column(name = "member_ids", columnDefinition = "JSON")
    private String memberIdsJson;

    // Relationships
    @OneToMany(mappedBy = "group", cascade = {})
    @JsonIgnore
    private Set<User> members = new HashSet<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Document> documents = new HashSet<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Grade> grades = new HashSet<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<RubricGrade> rubricGrades = new HashSet<>();

    // Helper method to get member IDs as List from JSON
    @Transient
    public List<Long> getMemberIds() {
        if (members != null && !members.isEmpty()) {
            // If members are loaded, compute from relationship
            return members.stream()
                    .map(User::getId)
                    .collect(Collectors.toList());
        } else if (memberIdsJson != null && !memberIdsJson.isEmpty()) {
            // If members not loaded, parse from JSON
            try {
                // Simple JSON array parsing: [1,2,3] -> List<Long>
                String json = memberIdsJson.trim();
                if (json.startsWith("[") && json.endsWith("]")) {
                    json = json.substring(1, json.length() - 1);
                    if (json.isEmpty()) {
                        return new ArrayList<>();
                    }
                    String[] parts = json.split(",");
                    List<Long> ids = new ArrayList<>();
                    for (String part : parts) {
                        try {
                            ids.add(Long.parseLong(part.trim()));
                        } catch (NumberFormatException e) {
                            // Skip invalid numbers
                        }
                    }
                    return ids;
                }
            } catch (Exception e) {
                // If parsing fails, return empty list
            }
        }
        return new ArrayList<>();
    }

    // Helper method to update memberIdsJson from members set
    public void updateMemberIdsJson() {
        if (members != null && !members.isEmpty()) {
            List<Long> ids = members.stream()
                    .map(User::getId)
                    .sorted()
                    .collect(Collectors.toList());
            // Convert to JSON array format: [1,2,3]
            this.memberIdsJson = "[" + ids.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(",")) + "]";
        } else {
            this.memberIdsJson = "[]";
        }
    }
}
