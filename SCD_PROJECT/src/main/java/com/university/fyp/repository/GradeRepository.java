package com.university.fyp.repository;

import com.university.fyp.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {

    List<Grade> findByGroupId(Long groupId);

    List<Grade> findByDocumentId(Long documentId);

    List<Grade> findByGradedById(Long gradedById);

    List<Grade> findByIsFinal(Boolean isFinal);

    List<Grade> findByGroupIdAndIsFinal(Long groupId, Boolean isFinal);

    @Query("SELECT g FROM Grade g WHERE g.group.id = :groupId AND g.isFinal = true")
    List<Grade> findFinalGradesByGroupId(@Param("groupId") Long groupId);

    Optional<Grade> findByDocumentIdAndIsFinal(Long documentId, Boolean isFinal);
}
