package com.university.fyp.repository;

import com.university.fyp.entity.VersionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VersionHistoryRepository extends JpaRepository<VersionHistory, Long> {

    List<VersionHistory> findByDocumentId(Long documentId);

    List<VersionHistory> findByDocumentIdOrderByVersionNumberDesc(Long documentId);

    List<VersionHistory> findByUploadedById(Long uploadedById);
}
