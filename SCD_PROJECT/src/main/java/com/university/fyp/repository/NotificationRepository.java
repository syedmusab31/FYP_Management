package com.university.fyp.repository;

import com.university.fyp.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserId(Long userId);

    List<Notification> findByUserIdAndIsRead(Long userId, Boolean isRead);

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByType(Notification.NotificationType type);

    Long countByUserIdAndIsRead(Long userId, Boolean isRead);

    void deleteByUserId(Long userId);
}
