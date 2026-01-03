package com.university.fyp.repository;

import com.university.fyp.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByGroupId(Long groupId);

    List<Document> findByStatus(Document.DocumentStatus status);

    List<Document> findByType(Document.DocumentType type);

    List<Document> findByGroupIdAndType(Long groupId, Document.DocumentType type);

    List<Document> findByGroupIdAndStatus(Long groupId, Document.DocumentStatus status);

    @Query("SELECT d FROM Document d WHERE d.group.supervisor.id = :supervisorId")
    List<Document> findBySupervisorId(@Param("supervisorId") Long supervisorId);

    @Query("SELECT d FROM Document d WHERE d.status = :status AND d.group.supervisor.id = :supervisorId")
    List<Document> findByStatusAndSupervisorId(@Param("status") Document.DocumentStatus status,
            @Param("supervisorId") Long supervisorId);

    @Query("SELECT COUNT(d) FROM Document d WHERE d.group.id = :groupId AND d.status = :status")
    Long countByGroupIdAndStatus(@Param("groupId") Long groupId, @Param("status") Document.DocumentStatus status);

    @Query("SELECT COUNT(d) FROM Document d WHERE d.status = :status")
    Long countByStatus(@Param("status") Document.DocumentStatus status);
}
