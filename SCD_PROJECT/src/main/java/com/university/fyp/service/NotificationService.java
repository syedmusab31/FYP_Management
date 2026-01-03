package com.university.fyp.service;

import com.university.fyp.entity.Notification;
import com.university.fyp.entity.User;
import com.university.fyp.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final AuthService authService;

    @Transactional
    public Notification createNotification(User user, String message,
            Notification.NotificationType type,
            String relatedEntityType, Long relatedEntityId) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setType(type);
        notification.setIsRead(false);
        notification.setRelatedEntityType(relatedEntityType);
        notification.setRelatedEntityId(relatedEntityId);

        return notificationRepository.save(notification);
    }

    public List<Notification> getUserNotifications() {
        User currentUser = authService.getCurrentUser();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());
    }

    public List<Notification> getUnreadNotifications() {
        User currentUser = authService.getCurrentUser();
        return notificationRepository.findByUserIdAndIsRead(currentUser.getId(), false);
    }

    public Long getUnreadCount() {
        User currentUser = authService.getCurrentUser();
        return notificationRepository.countByUserIdAndIsRead(currentUser.getId(), false);
    }

    @Transactional
    public Notification markAsRead(Long notificationId) {
        User currentUser = authService.getCurrentUser();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You don't have permission to mark this notification as read");
        }

        notification.setIsRead(true);
        return notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead() {
        User currentUser = authService.getCurrentUser();
        List<Notification> unreadNotifications = notificationRepository
                .findByUserIdAndIsRead(currentUser.getId(), false);

        unreadNotifications.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }

    @Transactional
    public void deleteNotification(Long notificationId) {
        User currentUser = authService.getCurrentUser();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You don't have permission to delete this notification");
        }

        notificationRepository.deleteById(notificationId);
    }

    @Transactional
    public void deleteAllNotifications() {
        User currentUser = authService.getCurrentUser();
        notificationRepository.deleteByUserId(currentUser.getId());
    }
}
