package com.university.fyp.repository;

import com.university.fyp.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByDocumentId(Long documentId);

    List<Review> findByReviewerId(Long reviewerId);

    List<Review> findByStatus(Review.ReviewStatus status);

    List<Review> findByDocumentIdAndReviewerId(Long documentId, Long reviewerId);
}
