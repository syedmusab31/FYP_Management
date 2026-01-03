package com.university.fyp.service;

import com.university.fyp.dto.DocumentDTO;
import com.university.fyp.entity.*;
import com.university.fyp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final GroupRepository groupRepository;
    private final DeadlineRepository deadlineRepository;
    private final VersionHistoryRepository versionHistoryRepository;
    private final ReviewRepository reviewRepository;
    private final NotificationService notificationService;
    private final AuthService authService;

    private static final String UPLOAD_DIR = "uploads/documents/";

    @Transactional
    public Document uploadDocument(Long groupId, String title, Document.DocumentType type,
            MultipartFile file, String changeDescription, Long deadlineId) throws IOException {
        User currentUser = authService.getCurrentUser();
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        // Trigger lazy loading of members to ensure they're available for permission
        // check
        if (group.getMembers() != null) {
            group.getMembers().size(); // Force load members
        }

        // Verify user has permission to upload for this group
        if (!canUserAccessGroup(currentUser, group)) {
            throw new RuntimeException("You don't have permission to upload documents for this group");
        }

        // Check if document already exists for this group and type
        List<Document> existingDocs = documentRepository.findByGroupIdAndType(groupId, type);
        Document document;
        int version = 1;

        if (!existingDocs.isEmpty()) {
            // Update existing document
            document = existingDocs.get(0);

            // Can only upload if document is in DRAFT or REVISION_REQUESTED status
            if (document.getStatus() != Document.DocumentStatus.DRAFT &&
                    document.getStatus() != Document.DocumentStatus.REVISION_REQUESTED) {
                throw new RuntimeException("Cannot upload new version. Document is " + document.getStatus());
            }

            // Version handling:
            // - If status is REVISION_REQUESTED: increment version (new revision)
            // - If status is DRAFT: keep same version (overwrite current draft)
            if (document.getStatus() == Document.DocumentStatus.REVISION_REQUESTED) {
                version = document.getVersion() + 1;
                document.setVersion(version);
            } else {
                // DRAFT status: overwrite file, same version
                version = document.getVersion();
            }

            document.setStatus(Document.DocumentStatus.DRAFT);
        } else {
            // Create new document
            document = new Document();
            document.setGroup(group);
            document.setTitle(title);
            document.setType(type);
            document.setVersion(version);
            document.setStatus(Document.DocumentStatus.DRAFT);
            document.setUploadedBy(currentUser);
        }

        // Save file
        String filePath = saveFile(file, groupId, type, version);
        document.setFilePath(filePath);

        // Set deadline if provided
        // Determine deadline logic
        if (deadlineId != null) {
            Deadline deadline = deadlineRepository.findById(deadlineId)
                    .orElseThrow(() -> new RuntimeException("Deadline not found"));

            // Validate deadline has not passed
            if (Instant.now().isAfter(deadline.getDueDate())) {
                throw new RuntimeException(
                        "Cannot upload document: The deadline '" + deadline.getTitle() + "' has passed.");
            }
            document.setDeadline(deadline);
        } else {
            // If no explicit deadline provided, try to auto-link active deadline for this
            // document type
            if (document.getDeadline() == null) {
                deadlineRepository.findByDocumentTypeAndIsActive(type, true).ifPresent(deadline -> {
                    document.setDeadline(deadline);
                });
            }

            // Check against the deadline (either existing or auto-linked)
            if (document.getDeadline() != null) {
                if (Instant.now().isAfter(document.getDeadline().getDueDate())) {
                    throw new RuntimeException("Cannot upload document: The deadline '"
                            + document.getDeadline().getTitle() + "' has passed.");
                }
            }
        }

        Document savedDocument = documentRepository.save(document);

        // Create version history entry
        createVersionHistory(savedDocument, version, filePath, changeDescription, currentUser);

        // Notify supervisor about document upload/resubmission
        if (savedDocument.getGroup().getSupervisor() != null) {
            String notificationMessage;
            Notification.NotificationType notificationType;
            
            if (version > 1) {
                // Resubmission
                notificationMessage = String.format("Group %s resubmitted document: %s (Version %d)",
                        savedDocument.getGroup().getGroupName(),
                        savedDocument.getTitle(),
                        version);
                notificationType = Notification.NotificationType.DOCUMENT_RESUBMITTED;
            } else {
                // Initial submission
                notificationMessage = String.format("Group %s uploaded document: %s for review",
                        savedDocument.getGroup().getGroupName(),
                        savedDocument.getTitle());
                notificationType = Notification.NotificationType.DOCUMENT_UPLOADED;
            }
            
            notificationService.createNotification(
                    savedDocument.getGroup().getSupervisor(),
                    notificationMessage,
                    notificationType,
                    "Document",
                    savedDocument.getId());
        }

        // Trigger lazy loading before returning
        triggerLazyLoading(savedDocument);

        return savedDocument;
    }

    /**
     * Triggers lazy loading of relationships to avoid proxy serialization issues
     */
    private void triggerLazyLoading(Document document) {
        if (document.getGroup() != null) {
            document.getGroup().getId();
            document.getGroup().getGroupName();
        }
        if (document.getUploadedBy() != null) {
            document.getUploadedBy().getId();
            document.getUploadedBy().getFullName();
            document.getUploadedBy().getEmail();
        }
        if (document.getDeadline() != null) {
            document.getDeadline().getId();
            document.getDeadline().getTitle();
        }
    }

    @Transactional
    public Document submitDocument(Long documentId) {
        User currentUser = authService.getCurrentUser();
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        // Verify user has permission
        if (!canUserAccessGroup(currentUser, document.getGroup())) {
            throw new RuntimeException("You don't have permission to submit this document");
        }

        // Can only submit if in DRAFT status
        // Students must upload a new version before resubmitting after
        // REVISION_REQUESTED
        if (document.getStatus() != Document.DocumentStatus.DRAFT) {
            throw new RuntimeException("Document cannot be submitted. Current status: " + document.getStatus() +
                    ". Please upload a new version first if the document requires revision.");
        }

        // Update status and submission time
        document.setStatus(Document.DocumentStatus.SUBMITTED);
        document.setSubmittedAt(Instant.now());

        // Check if submission is late
        if (document.getDeadline() != null) {
            boolean isLate = document.getSubmittedAt().isAfter(document.getDeadline().getDueDate());
            document.setIsLate(isLate);
        }

        Document savedDocument = documentRepository.save(document);

        // Notify supervisor
        if (document.getGroup().getSupervisor() != null) {
            notificationService.createNotification(
                    document.getGroup().getSupervisor(),
                    "New document submitted: " + document.getTitle() + " by " + document.getGroup().getGroupName(),
                    Notification.NotificationType.GENERAL,
                    "Document",
                    document.getId());
        }

        // Trigger lazy loading before returning
        triggerLazyLoading(savedDocument);

        return savedDocument;
    }

    @Transactional
    public Document updateDocumentStatus(Long documentId, Document.DocumentStatus newStatus) {
        User currentUser = authService.getCurrentUser();
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        // Only supervisors and committee members can change status
        String roleName = currentUser.getRole().getName();
        if (!roleName.equals("SUPERVISOR") && !roleName.equals("COMMITTEE_MEMBER") &&
                !roleName.equals("FYP_COMMITTEE")) {
            throw new RuntimeException("Only supervisors and committee members can update document status");
        }

        // Validate state transitions
        validateStatusTransition(document.getStatus(), newStatus);

        document.setStatus(newStatus);
        Document savedDocument = documentRepository.save(document);

        // Notify group members
        notifyGroupMembers(document, newStatus);

        // Trigger lazy loading before returning
        triggerLazyLoading(savedDocument);

        return savedDocument;
    }

    private void validateStatusTransition(Document.DocumentStatus currentStatus, Document.DocumentStatus newStatus) {
        // DRAFT -> SUBMITTED (done by student)
        // SUBMITTED -> UNDER_REVIEW (done by supervisor/committee)
        // UNDER_REVIEW -> APPROVED or REVISION_REQUESTED (done by supervisor/committee)
        // APPROVED -> GRADED (done by committee)
        // REVISION_REQUESTED -> DRAFT (automatic when new version uploaded)

        switch (currentStatus) {
            case SUBMITTED:
                if (newStatus != Document.DocumentStatus.UNDER_REVIEW) {
                    throw new RuntimeException("Submitted documents can only move to UNDER_REVIEW");
                }
                break;
            case UNDER_REVIEW:
                if (newStatus != Document.DocumentStatus.APPROVED &&
                        newStatus != Document.DocumentStatus.REVISION_REQUESTED) {
                    throw new RuntimeException("Documents under review can only be APPROVED or REVISION_REQUESTED");
                }
                break;
            case APPROVED:
                if (newStatus != Document.DocumentStatus.GRADED) {
                    throw new RuntimeException("Approved documents can only move to GRADED");
                }
                break;
            default:
                throw new RuntimeException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }
    }

    private void createVersionHistory(Document document, int version, String filePath,
            String changeDescription, User uploadedBy) {
        VersionHistory versionHistory = new VersionHistory();
        versionHistory.setDocument(document);
        versionHistory.setVersionNumber(version);
        versionHistory.setFilePath(filePath);
        versionHistory.setChangeDescription(changeDescription);
        versionHistory.setUploadedBy(uploadedBy);
        versionHistoryRepository.save(versionHistory);
    }

    private String saveFile(MultipartFile file, Long groupId, Document.DocumentType type, int version)
            throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";

        String filename = String.format("group_%d_%s_v%d_%s%s",
                groupId, type, version, UUID.randomUUID().toString(), extension);

        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filePath.toString();
    }

    private boolean canUserAccessGroup(User user, Group group) {
        String roleName = user.getRole().getName();

        // Students can only access their own group and must be a member of that group
        if (roleName.equals("STUDENT")) {
            // First check: user must have a group assigned
            if (user.getGroup() == null) {
                return false;
            }

            // Second check: user's group ID must match the requested group ID
            if (!user.getGroup().getId().equals(group.getId())) {
                return false;
            }

            // Third check: verify user is actually in the group's members set (if members
            // are loaded)
            // If members are not loaded (lazy), we trust the foreign key relationship
            if (group.getMembers() != null && !group.getMembers().isEmpty()) {
                // Members are loaded, verify user is in the set
                boolean isMember = group.getMembers().stream()
                        .anyMatch(member -> member.getId().equals(user.getId()));
                if (!isMember) {
                    return false;
                }
            }
            // If members are not loaded, we rely on the foreign key relationship
            // (user.group_id = group.id)
            // which is already validated above

            return true;
        }

        // Supervisors can access groups they supervise
        if (roleName.equals("SUPERVISOR")) {
            return group.getSupervisor() != null && group.getSupervisor().getId().equals(user.getId());
        }

        // Committee members and FYP committee can access all groups
        return roleName.equals("COMMITTEE_MEMBER") || roleName.equals("FYP_COMMITTEE");
    }

    private void notifyGroupMembers(Document document, Document.DocumentStatus newStatus) {
        Group group = document.getGroup();
        String message = String.format("Document '%s' status updated to %s",
                document.getTitle(), newStatus);

        Notification.NotificationType notificationType = switch (newStatus) {
            case APPROVED -> Notification.NotificationType.DOCUMENT_APPROVED;
            case REVISION_REQUESTED -> Notification.NotificationType.REVISION_REQUESTED;
            default -> Notification.NotificationType.GENERAL;
        };

        // Notify all group members
        group.getMembers().forEach(member -> notificationService.createNotification(member, message, notificationType,
                "Document", document.getId()));
    }

    public List<Document> getDocumentsByGroup(Long groupId) {
        User currentUser = authService.getCurrentUser();
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        if (!canUserAccessGroup(currentUser, group)) {
            throw new RuntimeException("You don't have permission to view documents for this group");
        }

        List<Document> documents = documentRepository.findByGroupId(groupId);
        // Trigger lazy loading for all documents
        documents.forEach(this::triggerLazyLoading);
        return documents;
    }

    public List<Document> getDocumentsBySupervisor(Long supervisorId) {
        List<Document> documents = documentRepository.findBySupervisorId(supervisorId);
        // Trigger lazy loading for all documents
        documents.forEach(this::triggerLazyLoading);
        return documents;
    }

    public List<Document> getAllDocumentsByStatus(Document.DocumentStatus status) {
        User currentUser = authService.getCurrentUser();
        String roleName = currentUser.getRole().getName();

        // Only committee members and FYP committee can view all documents
        if (!roleName.equals("COMMITTEE_MEMBER") && !roleName.equals("FYP_COMMITTEE")) {
            throw new RuntimeException("You don't have permission to view all documents");
        }

        List<Document> documents = documentRepository.findByStatus(status);
        // Trigger lazy loading for all documents
        documents.forEach(this::triggerLazyLoading);
        return documents;
    }

    public Document getDocumentById(Long documentId) {
        User currentUser = authService.getCurrentUser();
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (!canUserAccessGroup(currentUser, document.getGroup())) {
            throw new RuntimeException("You don't have permission to view this document");
        }

        // Trigger lazy loading before returning
        triggerLazyLoading(document);

        return document;
    }

    // DTO conversion methods
    public DocumentDTO getDocumentDTOById(Long documentId) {
        Document document = getDocumentById(documentId);
        return DocumentDTO.fromDocument(document);
    }

    public List<DocumentDTO> getDocumentDTOsByGroup(Long groupId) {
        List<Document> documents = getDocumentsByGroup(groupId);
        return documents.stream()
                .map(DocumentDTO::fromDocument)
                .collect(Collectors.toList());
    }

    public List<DocumentDTO> getDocumentDTOsBySupervisor(Long supervisorId) {
        List<Document> documents = getDocumentsBySupervisor(supervisorId);
        return documents.stream()
                .map(DocumentDTO::fromDocument)
                .collect(Collectors.toList());
    }

    public List<DocumentDTO> getAllDocumentDTOsByStatus(Document.DocumentStatus status) {
        List<Document> documents = getAllDocumentsByStatus(status);
        return documents.stream()
                .map(DocumentDTO::fromDocument)
                .collect(Collectors.toList());
    }

    public List<VersionHistory> getDocumentVersionHistory(Long documentId) {
        User currentUser = authService.getCurrentUser();
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (!canUserAccessGroup(currentUser, document.getGroup())) {
            throw new RuntimeException("You don't have permission to view this document's history");
        }

        return versionHistoryRepository.findByDocumentIdOrderByVersionNumberDesc(documentId);
    }

    @Transactional
    public Document reviewDocument(Long documentId, String action, String comments) {
        User currentUser = authService.getCurrentUser();
        String roleName = currentUser.getRole().getName();

        // Business Rule 1: SUPERVISOR or COMMITTEE_MEMBER can review
        if (!roleName.equals("SUPERVISOR") && !roleName.equals("COMMITTEE_MEMBER")) {
            throw new RuntimeException("Only supervisors and committee members can review documents");
        }

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        Group group = document.getGroup();

        // Business Rule 2: Validation based on role
        if (roleName.equals("SUPERVISOR")) {
            if (document.getStatus() != Document.DocumentStatus.SUBMITTED) {
                throw new RuntimeException("Supervisors can only review SUBMITTED documents.");
            }
            // Supervisor must own group
            if (group.getSupervisor() == null || !group.getSupervisor().getId().equals(currentUser.getId())) {
                throw new RuntimeException("You can only review documents from groups you supervise");
            }
        } else {
            // COMMITTEE_MEMBER
            if (document.getStatus() != Document.DocumentStatus.APPROVED) {
                throw new RuntimeException("Committee members can only request revision on APPROVED documents.");
            }
            // Committee can access all (or assigned) - currently all
        }

        // Validate action
        if (!action.equalsIgnoreCase("APPROVE") && !action.equalsIgnoreCase("REVISION")) {
            throw new RuntimeException("Invalid review action. Must be 'APPROVE' or 'REVISION'");
        }

        // Business Rule 4: Update document status based on action
        Document.DocumentStatus newStatus;
        Review.ReviewStatus reviewStatus;

        if (action.equalsIgnoreCase("APPROVE")) {
            newStatus = Document.DocumentStatus.APPROVED;
            reviewStatus = Review.ReviewStatus.APPROVED;
        } else { // REVISION
            newStatus = Document.DocumentStatus.REVISION_REQUESTED;
            reviewStatus = Review.ReviewStatus.REVISION_REQUESTED;
        }

        document.setStatus(newStatus);
        Document savedDocument = documentRepository.save(document);

        // Business Rule 5: Every review must be logged
        Review review = new Review();
        review.setDocument(savedDocument);
        review.setReviewer(currentUser);
        review.setComments(comments != null ? comments : "");
        review.setStatus(reviewStatus);
        // reviewedAt is automatically set by @CreatedDate annotation
        reviewRepository.save(review);

        // Notify group members
        String message = String.format("Document '%s' has been reviewed: %s",
                document.getTitle(), action.equalsIgnoreCase("APPROVE") ? "APPROVED" : "REVISION REQUIRED");
        Notification.NotificationType notificationType = action.equalsIgnoreCase("APPROVE")
                ? Notification.NotificationType.DOCUMENT_APPROVED
                : Notification.NotificationType.REVISION_REQUESTED;

        group.getMembers().forEach(member -> notificationService.createNotification(
                member,
                message,
                notificationType,
                "Document",
                savedDocument.getId()));

        // Trigger lazy loading before returning
        triggerLazyLoading(savedDocument);

        return savedDocument;
    }
}
