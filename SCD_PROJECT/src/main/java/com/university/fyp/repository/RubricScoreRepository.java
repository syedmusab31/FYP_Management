package com.university.fyp.repository;

import com.university.fyp.entity.RubricScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RubricScoreRepository extends JpaRepository<RubricScore, Long> {
    List<RubricScore> findByRubricGradeId(Long rubricGradeId);
    List<RubricScore> findByCriteriaId(Long criteriaId);
}

