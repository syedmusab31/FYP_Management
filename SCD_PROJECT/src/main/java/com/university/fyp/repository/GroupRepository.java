package com.university.fyp.repository;

import com.university.fyp.entity.Group;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    Optional<Group> findByGroupName(String groupName);

    List<Group> findBySupervisorId(Long supervisorId);

    boolean existsByGroupName(String groupName);

    // Fetch group with supervisor and members loaded
    @EntityGraph(attributePaths = {"supervisor", "members"})
    Optional<Group> findById(Long id);

    // Fetch all groups with supervisor and members loaded
    @EntityGraph(attributePaths = {"supervisor", "members"})
    @Override
    List<Group> findAll();
}
