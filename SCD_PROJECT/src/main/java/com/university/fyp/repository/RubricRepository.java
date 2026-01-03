package com.university.fyp.repository;

import com.university.fyp.entity.Rubric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RubricRepository extends JpaRepository<Rubric, Long> {
    List<Rubric> findByDocumentTypeAndIsActive(com.university.fyp.entity.Document.DocumentType documentType, Boolean isActive);
    List<Rubric> findByIsActive(Boolean isActive);
    Optional<Rubric> findByIdAndIsActive(Long id, Boolean isActive);
}

