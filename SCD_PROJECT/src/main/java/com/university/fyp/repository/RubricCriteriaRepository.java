package com.university.fyp.repository;

import com.university.fyp.entity.RubricCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RubricCriteriaRepository extends JpaRepository<RubricCriteria, Long> {
    List<RubricCriteria> findByRubricIdOrderByOrderIndex(Long rubricId);
}

