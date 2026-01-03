package com.university.fyp.repository;

import com.university.fyp.entity.RubricGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RubricGradeRepository extends JpaRepository<RubricGrade, Long> {
    List<RubricGrade> findByGroupId(Long groupId);
    List<RubricGrade> findByGroupIdAndIsFinal(Long groupId, Boolean isFinal);
    List<RubricGrade> findByDocumentId(Long documentId);
    List<RubricGrade> findByRubricId(Long rubricId);
    
    @EntityGraph(attributePaths = {"rubric", "group", "document", "gradedBy", "scores"})
    Optional<RubricGrade> findById(Long id);
}

