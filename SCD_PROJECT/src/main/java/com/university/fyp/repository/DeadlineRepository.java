package com.university.fyp.repository;

import com.university.fyp.entity.Deadline;
import com.university.fyp.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeadlineRepository extends JpaRepository<Deadline, Long> {

    List<Deadline> findByIsActive(Boolean isActive);

    List<Deadline> findByDocumentType(Document.DocumentType documentType);

    Optional<Deadline> findByDocumentTypeAndIsActive(Document.DocumentType documentType, Boolean isActive);

    @Query("SELECT d FROM Deadline d WHERE d.dueDate > :now AND d.isActive = true ORDER BY d.dueDate ASC")
    List<Deadline> findUpcomingDeadlines(@Param("now") Instant now);

    @Query("SELECT d FROM Deadline d WHERE d.dueDate BETWEEN :start AND :end AND d.isActive = true")
    List<Deadline> findDeadlinesBetween(@Param("start") Instant start, @Param("end") Instant end);
}
