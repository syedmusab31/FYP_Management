package com.university.fyp.service;

import com.university.fyp.entity.*;
import com.university.fyp.repository.DocumentRepository;
import com.university.fyp.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final DocumentRepository documentRepository;
    private final NotificationService notificationService;
    private final AuthService authService;

    @Transactional
    public Review createReview(Long documentId, String comments, Review.ReviewStatus status) {
        User currentUser = authService.getCurrentUser();
        String roleName = currentUser.getRole().getName();

        // Only supervisors and committee members can create reviews
        if (!roleName.equals("SUPERVISOR") && !roleName.equals("COMMITTEE_MEMBER")) {
            throw new RuntimeException("Only supervisors and committee members can review documents");
        }

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        // Supervisors can only review documents from their supervised groups
        if (roleName.equals("SUPERVISOR")) {
            if (document.getGroup().getSupervisor() == null ||
                    !document.getGroup().getSupervisor().getId().equals(currentUser.getId())) {
                throw new RuntimeException("You can only review documents from groups you supervise");
            }
        }

        // Document must be in SUBMITTED or UNDER_REVIEW status
        if (document.getStatus() != Document.DocumentStatus.SUBMITTED &&
                document.getStatus() != Document.DocumentStatus.UNDER_REVIEW) {
            throw new RuntimeException("Document must be submitted before it can be reviewed");
        }

        // Update document status based on review
        if (document.getStatus() == Document.DocumentStatus.SUBMITTED) {
            document.setStatus(Document.DocumentStatus.UNDER_REVIEW);
        }

        Review review = new Review();
        review.setDocument(document);
        review.setReviewer(currentUser);
        review.setComments(comments);
        review.setStatus(status);

        Review savedReview = reviewRepository.save(review);

        // Update document status based on review outcome
        if (status == Review.ReviewStatus.APPROVED) {
            document.setStatus(Document.DocumentStatus.APPROVED);
        } else if (status == Review.ReviewStatus.REVISION_REQUESTED) {
            document.setStatus(Document.DocumentStatus.REVISION_REQUESTED);
        }
        documentRepository.save(document);

        // Notify group members
        String message = String.format("Review completed for '%s': %s",
                document.getTitle(), status);
        Notification.NotificationType notificationType = status == Review.ReviewStatus.APPROVED
                ? Notification.NotificationType.DOCUMENT_APPROVED
                : Notification.NotificationType.REVISION_REQUESTED;

        document.getGroup().getMembers().forEach(member -> notificationService.createNotification(
                member,
                message,
                notificationType,
                "Review",
                savedReview.getId()));

        // Notify supervisor if review is by committee and revision is requested
        if (roleName.equals("COMMITTEE_MEMBER") && status == Review.ReviewStatus.REVISION_REQUESTED) {
            if (document.getGroup().getSupervisor() != null) {
                String supervisorMessage = String.format("Committee requested revision for '%s' by group %s",
                        document.getTitle(),
                        document.getGroup().getGroupName());
                notificationService.createNotification(
                        document.getGroup().getSupervisor(),
                        supervisorMessage,
                        Notification.NotificationType.COMMITTEE_REVISION_REQUESTED,
                        "Review",
                        savedReview.getId());
            }
        }

        return savedReview;
    }

    public List<Review> getReviewsByDocument(Long documentId) {
        User currentUser = authService.getCurrentUser();
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        // Check if user has permission to view reviews
        String roleName = currentUser.getRole().getName();
        if (roleName.equals("STUDENT")) {
            if (currentUser.getGroup() == null ||
                    !currentUser.getGroup().getId().equals(document.getGroup().getId())) {
                throw new RuntimeException("You don't have permission to view reviews for this document");
            }
        } else if (roleName.equals("SUPERVISOR")) {
            if (document.getGroup().getSupervisor() == null ||
                    !document.getGroup().getSupervisor().getId().equals(currentUser.getId())) {
                throw new RuntimeException("You don't have permission to view reviews for this document");
            }
        }

        return reviewRepository.findByDocumentId(documentId);
    }

    public List<Review> getReviewsByReviewer(Long reviewerId) {
        User currentUser = authService.getCurrentUser();

        // Users can only see their own reviews unless they are committee members
        if (!currentUser.getId().equals(reviewerId) &&
                !currentUser.getRole().getName().equals("COMMITTEE_MEMBER") &&
                !currentUser.getRole().getName().equals("FYP_COMMITTEE")) {
            throw new RuntimeException("You don't have permission to view these reviews");
        }

        return reviewRepository.findByReviewerId(reviewerId);
    }

    public Review getReviewById(Long reviewId) {
        User currentUser = authService.getCurrentUser();
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        // Check permissions
        String roleName = currentUser.getRole().getName();
        if (roleName.equals("STUDENT")) {
            if (currentUser.getGroup() == null ||
                    !currentUser.getGroup().getId().equals(review.getDocument().getGroup().getId())) {
                throw new RuntimeException("You don't have permission to view this review");
            }
        } else if (roleName.equals("SUPERVISOR")) {
            Group group = review.getDocument().getGroup();
            if (group.getSupervisor() == null ||
                    !group.getSupervisor().getId().equals(currentUser.getId())) {
                throw new RuntimeException("You don't have permission to view this review");
            }
        }

        return review;
    }
}
