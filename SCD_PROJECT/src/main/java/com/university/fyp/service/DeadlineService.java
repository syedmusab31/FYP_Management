package com.university.fyp.service;

import com.university.fyp.dto.DeadlineDTO;
import com.university.fyp.entity.Deadline;
import com.university.fyp.entity.Notification;
import com.university.fyp.entity.User;
import com.university.fyp.repository.DeadlineRepository;
import com.university.fyp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeadlineService {

    private final DeadlineRepository deadlineRepository;
    private final AuthService authService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public List<DeadlineDTO> getAllDeadlines() {
        return deadlineRepository.findAll().stream()
                .map(DeadlineDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<DeadlineDTO> getActiveDeadlines() {
        return deadlineRepository.findByIsActive(true).stream()
                .map(DeadlineDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public DeadlineDTO createDeadline(DeadlineDTO dto) {
        User currentUser = authService.getCurrentUser();
        if (!currentUser.getRole().getName().equals("FYP_COMMITTEE")) {
            throw new RuntimeException("Only FYP Committee can create deadlines");
        }

        // Basic validation
        if (dto.getDueDate() != null && dto.getDueDate().isBefore(Instant.now())) {
            throw new RuntimeException("Deadline due date cannot be in the past");
        }

        Deadline deadline = new Deadline();
        deadline.setTitle(dto.getTitle());
        deadline.setDescription(dto.getDescription());
        deadline.setDocumentType(dto.getDocumentType());
        deadline.setDueDate(dto.getDueDate());
        deadline.setIsActive(true);

        Deadline saved = deadlineRepository.save(deadline);

        // Notify all students about the new deadline
        List<User> allStudents = userRepository.findByRoleName("STUDENT");
        String notificationMessage = String.format("New deadline created: %s - Due on %s",
                deadline.getTitle(),
                deadline.getDueDate().toString());

        allStudents.forEach(student -> notificationService.createNotification(
                student,
                notificationMessage,
                Notification.NotificationType.DEADLINE_CREATED,
                "Deadline",
                saved.getId()));

        return DeadlineDTO.fromEntity(saved);
    }

    @Transactional
    public void deleteDeadline(Long id) {
        User currentUser = authService.getCurrentUser();
        if (!currentUser.getRole().getName().equals("FYP_COMMITTEE")) {
            throw new RuntimeException("Only FYP Committee can delete deadlines");
        }

        if (!deadlineRepository.existsById(id)) {
            throw new RuntimeException("Deadline not found");
        }

        deadlineRepository.deleteById(id);
    }
}
