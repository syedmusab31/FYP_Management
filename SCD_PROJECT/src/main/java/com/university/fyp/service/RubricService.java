package com.university.fyp.service;

import com.university.fyp.dto.*;
import com.university.fyp.entity.*;
import com.university.fyp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RubricService {

    private final RubricRepository rubricRepository;
    private final RubricCriteriaRepository criteriaRepository;
    private final RubricGradeRepository rubricGradeRepository;
    private final RubricScoreRepository rubricScoreRepository;
    private final GroupRepository groupRepository;
    private final DocumentRepository documentRepository;
    private final AuthService authService;

    @Transactional
    public RubricDTO createRubric(RubricRequest request) {
        User currentUser = authService.getCurrentUser();
        String roleName = currentUser.getRole().getName();

        // Only FYP_COMMITTEE can create rubrics
        if (!roleName.equals("FYP_COMMITTEE")) {
            throw new RuntimeException("Only FYP Committee members can create rubrics");
        }

        Rubric rubric = new Rubric();
        rubric.setTitle(request.getTitle());
        rubric.setDescription(request.getDescription());
        rubric.setDocumentType(request.getDocumentType());
        rubric.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        rubric.setCreatedBy(currentUser);

        Rubric savedRubric = rubricRepository.save(rubric);

        // Create criteria
        if (request.getCriteria() != null && !request.getCriteria().isEmpty()) {
            for (CriteriaRequest criteriaReq : request.getCriteria()) {
                RubricCriteria criteria = new RubricCriteria();
                criteria.setRubric(savedRubric);
                criteria.setCriterionName(criteriaReq.getCriterionName());
                criteria.setDescription(criteriaReq.getDescription());
                criteria.setMaxScore(criteriaReq.getMaxScore());
                criteria.setOrderIndex(criteriaReq.getOrderIndex());
                criteriaRepository.save(criteria);
            }
        }

        // Reload with criteria
        savedRubric = rubricRepository.findById(savedRubric.getId())
                .orElseThrow(() -> new RuntimeException("Rubric not found"));

        return RubricDTO.fromRubric(savedRubric);
    }

    public List<RubricDTO> getAllRubrics() {
        List<Rubric> rubrics = rubricRepository.findAll();
        return rubrics.stream()
                .map(RubricDTO::fromRubric)
                .collect(Collectors.toList());
    }

    public List<RubricDTO> getActiveRubrics() {
        List<Rubric> rubrics = rubricRepository.findByIsActive(true);
        return rubrics.stream()
                .map(RubricDTO::fromRubric)
                .collect(Collectors.toList());
    }

    public List<RubricDTO> getRubricsByDocumentType(com.university.fyp.entity.Document.DocumentType documentType) {
        List<Rubric> rubrics = rubricRepository.findByDocumentTypeAndIsActive(documentType, true);
        return rubrics.stream()
                .map(RubricDTO::fromRubric)
                .collect(Collectors.toList());
    }

    public RubricDTO getRubricById(Long id) {
        Rubric rubric = rubricRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rubric not found"));
        return RubricDTO.fromRubric(rubric);
    }

    @Transactional
    public RubricDTO updateRubric(Long id, RubricRequest request) {
        User currentUser = authService.getCurrentUser();
        String roleName = currentUser.getRole().getName();

        // Only FYP_COMMITTEE can update rubrics
        if (!roleName.equals("FYP_COMMITTEE")) {
            throw new RuntimeException("Only FYP Committee members can update rubrics");
        }

        Rubric rubric = rubricRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rubric not found"));

        rubric.setTitle(request.getTitle());
        rubric.setDescription(request.getDescription());
        rubric.setDocumentType(request.getDocumentType());
        if (request.getIsActive() != null) {
            rubric.setIsActive(request.getIsActive());
        }

        // Update criteria - remove old and add new
        criteriaRepository.deleteAll(rubric.getCriteria());
        rubric.getCriteria().clear();

        if (request.getCriteria() != null && !request.getCriteria().isEmpty()) {
            for (CriteriaRequest criteriaReq : request.getCriteria()) {
                RubricCriteria criteria = new RubricCriteria();
                criteria.setRubric(rubric);
                criteria.setCriterionName(criteriaReq.getCriterionName());
                criteria.setDescription(criteriaReq.getDescription());
                criteria.setMaxScore(criteriaReq.getMaxScore());
                criteria.setOrderIndex(criteriaReq.getOrderIndex());
                criteriaRepository.save(criteria);
            }
        }

        Rubric savedRubric = rubricRepository.save(rubric);
        return RubricDTO.fromRubric(savedRubric);
    }

    @Transactional
    public RubricGradeDTO assignRubricGrade(RubricGradeRequest request) {
        User currentUser = authService.getCurrentUser();
        String roleName = currentUser.getRole().getName();

        // Only COMMITTEE_MEMBER and FYP_COMMITTEE can assign rubric grades
        if (!roleName.equals("COMMITTEE_MEMBER") && !roleName.equals("FYP_COMMITTEE")) {
            throw new RuntimeException("Only committee members can assign rubric grades");
        }

        Rubric rubric = rubricRepository.findByIdAndIsActive(request.getRubricId(), true)
                .orElseThrow(() -> new RuntimeException("Active rubric not found"));

        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new RuntimeException("Group not found"));

        Document document = null;
        if (request.getDocumentId() != null) {
            document = documentRepository.findById(request.getDocumentId())
                    .orElseThrow(() -> new RuntimeException("Document not found"));
        }

        // Validate scores
        List<RubricCriteria> criteria = criteriaRepository.findByRubricIdOrderByOrderIndex(request.getRubricId());
        if (criteria.size() != request.getScores().size()) {
            throw new RuntimeException("Number of scores must match number of criteria");
        }

        // Calculate total score
        BigDecimal totalScore = BigDecimal.ZERO;
        for (ScoreRequest scoreReq : request.getScores()) {
            RubricCriteria criterion = criteria.stream()
                    .filter(c -> c.getId().equals(scoreReq.getCriteriaId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Invalid criteria ID: " + scoreReq.getCriteriaId()));

            if (scoreReq.getScore().compareTo(criterion.getMaxScore()) > 0) {
                throw new RuntimeException("Score cannot exceed max score for criterion: " + criterion.getCriterionName());
            }

            totalScore = totalScore.add(scoreReq.getScore());
        }

        // Create rubric grade
        RubricGrade rubricGrade = new RubricGrade();
        rubricGrade.setRubric(rubric);
        rubricGrade.setGroup(group);
        rubricGrade.setDocument(document);
        rubricGrade.setGradedBy(currentUser);
        rubricGrade.setTotalScore(totalScore);
        rubricGrade.setOverallFeedback(request.getOverallFeedback());
        rubricGrade.setIsFinal(request.getIsFinal() != null ? request.getIsFinal() : false);

        RubricGrade savedGrade = rubricGradeRepository.save(rubricGrade);

        // Create scores
        for (ScoreRequest scoreReq : request.getScores()) {
            RubricCriteria criterion = criteria.stream()
                    .filter(c -> c.getId().equals(scoreReq.getCriteriaId()))
                    .findFirst()
                    .orElseThrow();

            RubricScore score = new RubricScore();
            score.setRubricGrade(savedGrade);
            score.setCriteria(criterion);
            score.setScore(scoreReq.getScore());
            score.setFeedback(scoreReq.getFeedback());
            rubricScoreRepository.save(score);
        }

        // Reload with relationships
        savedGrade = rubricGradeRepository.findById(savedGrade.getId())
                .orElseThrow(() -> new RuntimeException("Rubric grade not found"));

        return RubricGradeDTO.fromRubricGrade(savedGrade);
    }

    public List<RubricGradeDTO> getRubricGradesByGroup(Long groupId) {
        List<RubricGrade> grades = rubricGradeRepository.findByGroupId(groupId);
        return grades.stream()
                .map(RubricGradeDTO::fromRubricGrade)
                .collect(Collectors.toList());
    }

    public List<RubricGradeDTO> getFinalRubricGradesByGroup(Long groupId) {
        List<RubricGrade> grades = rubricGradeRepository.findByGroupIdAndIsFinal(groupId, true);
        return grades.stream()
                .map(RubricGradeDTO::fromRubricGrade)
                .collect(Collectors.toList());
    }

    public RubricGradeDTO getRubricGradeById(Long id) {
        RubricGrade grade = rubricGradeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rubric grade not found"));
        return RubricGradeDTO.fromRubricGrade(grade);
    }
}

