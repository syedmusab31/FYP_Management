package com.university.fyp.controller;

import com.university.fyp.dto.AddMemberRequest;
import com.university.fyp.dto.AssignSupervisorRequest;
import com.university.fyp.dto.GroupRequest;
import com.university.fyp.dto.GroupResponse;
import com.university.fyp.dto.MessageResponse;
import com.university.fyp.entity.Group;
import com.university.fyp.entity.User;
import com.university.fyp.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    @PreAuthorize("hasRole('FYP_COMMITTEE')")
    public ResponseEntity<?> createGroup(@Valid @RequestBody GroupRequest groupRequest) {
        try {
            Group group = groupService.createGroup(groupRequest);
            return ResponseEntity.ok(GroupResponse.fromGroup(group));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }

    @PutMapping("/{groupId}")
    @PreAuthorize("hasRole('FYP_COMMITTEE')")
    public ResponseEntity<?> updateGroup(@PathVariable Long groupId,
            @Valid @RequestBody GroupRequest groupRequest) {
        try {
            Group group = groupService.updateGroup(groupId, groupRequest);
            return ResponseEntity.ok(GroupResponse.fromGroup(group));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<?> getGroup(@PathVariable Long groupId) {
        try {
            GroupResponse groupResponse = groupService.getGroupResponseById(groupId);
            return ResponseEntity.ok(groupResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }

    @GetMapping("/{groupId}/details")
    public ResponseEntity<?> getGroupDetails(@PathVariable Long groupId) {
        try {
            GroupResponse groupResponse = groupService.getGroupResponseById(groupId);
            return ResponseEntity.ok(groupResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllGroups() {
        try {
            List<GroupResponse> groupResponses = groupService.getAllGroupResponses();
            return ResponseEntity.ok(groupResponses);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }

    @GetMapping("/details")
    public ResponseEntity<?> getAllGroupsDetails() {
        try {
            List<GroupResponse> groupResponses = groupService.getAllGroupResponses();
            return ResponseEntity.ok(groupResponses);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error in getAllGroupsDetails: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }

    @DeleteMapping("/{groupId}")
    @PreAuthorize("hasRole('FYP_COMMITTEE')")
    public ResponseEntity<?> deleteGroup(@PathVariable Long groupId) {
        try {
            groupService.deleteGroup(groupId);
            return ResponseEntity.ok(new MessageResponse("Group deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }

    @PostMapping("/{groupId}/members")
    @PreAuthorize("hasRole('FYP_COMMITTEE')")
    public ResponseEntity<?> addMemberToGroup(@PathVariable Long groupId,
            @Valid @RequestBody AddMemberRequest addMemberRequest) {
        try {
            Group group = groupService.addMemberToGroup(groupId, addMemberRequest);
            return ResponseEntity.ok(GroupResponse.fromGroup(group));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    @PreAuthorize("hasRole('FYP_COMMITTEE')")
    public ResponseEntity<?> removeMemberFromGroup(@PathVariable Long groupId,
            @PathVariable Long userId) {
        try {
            Group group = groupService.removeMemberFromGroup(groupId, userId);
            return ResponseEntity.ok(GroupResponse.fromGroup(group));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<?> getGroupMembers(@PathVariable Long groupId) {
        try {
            List<User> members = groupService.getGroupMembers(groupId);
            return ResponseEntity.ok(members);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }

    @PutMapping("/{groupId}/supervisor")
    @PreAuthorize("hasRole('FYP_COMMITTEE')")
    public ResponseEntity<?> assignSupervisor(@PathVariable Long groupId,
            @Valid @RequestBody AssignSupervisorRequest request) {
        try {
            Group group = groupService.assignSupervisorToGroup(groupId, request.getSupervisorId());
            return ResponseEntity.ok(GroupResponse.fromGroup(group));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }

    @DeleteMapping("/{groupId}/supervisor")
    @PreAuthorize("hasRole('FYP_COMMITTEE')")
    public ResponseEntity<?> removeSupervisor(@PathVariable Long groupId) {
        try {
            Group group = groupService.removeSupervisorFromGroup(groupId);
            return ResponseEntity.ok(GroupResponse.fromGroup(group));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage(), false));
        }
    }
}
