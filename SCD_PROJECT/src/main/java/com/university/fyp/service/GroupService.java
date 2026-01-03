package com.university.fyp.service;

import com.university.fyp.dto.AddMemberRequest;
import com.university.fyp.dto.GroupRequest;
import com.university.fyp.dto.GroupResponse;
import com.university.fyp.entity.Group;
import com.university.fyp.entity.User;
import com.university.fyp.repository.GroupRepository;
import com.university.fyp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final AuthService authService;

    @Transactional
    public Group createGroup(GroupRequest groupRequest) {
        User currentUser = authService.getCurrentUser();

        // Only FYP_COMMITTEE can create groups
        if (!currentUser.getRole().getName().equals("FYP_COMMITTEE")) {
            throw new RuntimeException("Only FYP Committee members can create groups");
        }

        // Check if group name already exists
        if (groupRepository.existsByGroupName(groupRequest.getGroupName())) {
            throw new RuntimeException("Group name already exists");
        }

        Group group = new Group();
        group.setGroupName(groupRequest.getGroupName());
        group.setProjectTitle(groupRequest.getProjectTitle());
        group.setProjectDescription(groupRequest.getProjectDescription());

        // Assign supervisor if provided
        if (groupRequest.getSupervisorId() != null) {
            assignSupervisorToGroup(group, groupRequest.getSupervisorId());
        }

        // Save the group to database
        Group savedGroup = groupRepository.save(group);
        groupRepository.flush(); // Ensure immediate database write

        return savedGroup;
    }

    @Transactional
    public Group updateGroup(Long groupId, GroupRequest groupRequest) {
        User currentUser = authService.getCurrentUser();
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        // Only FYP_COMMITTEE can update groups
        if (!currentUser.getRole().getName().equals("FYP_COMMITTEE")) {
            throw new RuntimeException("Only FYP Committee members can update groups");
        }

        // Check if new group name conflicts with existing group (excluding current
        // group)
        if (!group.getGroupName().equals(groupRequest.getGroupName()) &&
                groupRepository.existsByGroupName(groupRequest.getGroupName())) {
            throw new RuntimeException("Group name already exists");
        }

        group.setGroupName(groupRequest.getGroupName());
        group.setProjectTitle(groupRequest.getProjectTitle());
        group.setProjectDescription(groupRequest.getProjectDescription());

        // Update supervisor if provided
        if (groupRequest.getSupervisorId() != null) {
            assignSupervisorToGroup(group, groupRequest.getSupervisorId());
        } else {
            // Remove supervisor if null is explicitly passed
            group.setSupervisor(null);
        }

        // Save the group to database
        Group savedGroup = groupRepository.save(group);
        groupRepository.flush(); // Ensure immediate database write

        return savedGroup;
    }

    public Group getGroupById(Long groupId) {
        User currentUser = authService.getCurrentUser();
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        // Check access permissions
        if (!canUserAccessGroup(currentUser, group)) {
            throw new RuntimeException("You don't have permission to view this group");
        }

        // Trigger lazy loading of relationships
        if (group.getSupervisor() != null) {
            group.getSupervisor().getId();
        }
        group.getMembers().size();

        return group;
    }

    public GroupResponse getGroupResponseById(Long groupId) {
        User currentUser = authService.getCurrentUser();
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        // Check access permissions
        if (!canUserAccessGroup(currentUser, group)) {
            throw new RuntimeException("You don't have permission to view this group");
        }

        // Trigger lazy loading of relationships
        if (group.getSupervisor() != null) {
            group.getSupervisor().getId();
        }
        group.getMembers().size();

        return GroupResponse.fromGroup(group);
    }

    public List<Group> getAllGroups() {
        User currentUser = authService.getCurrentUser();
        String roleName = currentUser.getRole().getName();

        // FYP_COMMITTEE can see all groups
        if (roleName.equals("FYP_COMMITTEE")) {
            List<Group> groups = groupRepository.findAll();
            // Trigger lazy loading for all groups
            groups.forEach(group -> {
                if (group.getSupervisor() != null) {
                    group.getSupervisor().getId();
                }
                group.getMembers().size();
            });
            return groups;
        }

        // SUPERVISOR can see only their supervised groups
        if (roleName.equals("SUPERVISOR")) {
            List<Group> groups = groupRepository.findBySupervisorId(currentUser.getId());
            // Load members and supervisor for each group
            return groups.stream()
                    .map(group -> {
                        // Trigger lazy loading of members and supervisor
                        group.getMembers().size();
                        if (group.getSupervisor() != null) {
                            group.getSupervisor().getId();
                        }
                        return group;
                    })
                    .collect(Collectors.toList());
        }

        // COMMITTEE_MEMBER can see all groups
        if (roleName.equals("COMMITTEE_MEMBER")) {
            List<Group> groups = groupRepository.findAll();
            // Trigger lazy loading for all groups
            groups.forEach(group -> {
                if (group.getSupervisor() != null) {
                    group.getSupervisor().getId();
                }
                group.getMembers().size();
            });
            return groups;
        }

        // STUDENT can see only their own group
        if (roleName.equals("STUDENT") && currentUser.getGroup() != null) {
            Group group = groupRepository.findById(currentUser.getGroup().getId())
                    .orElse(currentUser.getGroup());
            // Trigger lazy loading
            if (group.getSupervisor() != null) {
                group.getSupervisor().getId();
            }
            group.getMembers().size();
            return List.of(group);
        }

        return List.of();
    }

    public List<GroupResponse> getAllGroupResponses() {
        List<Group> groups = getAllGroups();
        return groups.stream()
                .map(GroupResponse::fromGroup)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteGroup(Long groupId) {
        User currentUser = authService.getCurrentUser();

        // Only FYP_COMMITTEE can delete groups
        if (!currentUser.getRole().getName().equals("FYP_COMMITTEE")) {
            throw new RuntimeException("Only FYP Committee members can delete groups");
        }

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        // Dissociate members from the group before deletion
        List<User> members = userRepository.findByGroupId(groupId);
        for (User member : members) {
            member.setGroup(null);
            userRepository.save(member);
        }

        // Ensure changes are written before group deletion
        userRepository.flush();

        groupRepository.delete(group);
    }

    @Transactional
    public Group addMemberToGroup(Long groupId, AddMemberRequest addMemberRequest) {
        User currentUser = authService.getCurrentUser();

        // Only FYP_COMMITTEE can add members
        if (!currentUser.getRole().getName().equals("FYP_COMMITTEE")) {
            throw new RuntimeException("Only FYP Committee members can add members to groups");
        }

        // Fetch and validate group
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        // Fetch and validate user
        User user = userRepository.findById(addMemberRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify user is a student
        if (!user.getRole().getName().equals("STUDENT")) {
            throw new RuntimeException("Only students can be added to groups");
        }

        // Check if user is already in a different group
        if (user.getGroup() != null && !user.getGroup().getId().equals(groupId)) {
            throw new RuntimeException("User is already a member of another group: " + user.getGroup().getGroupName());
        }

        // Check if user is already in this group
        if (user.getGroup() != null && user.getGroup().getId().equals(groupId)) {
            throw new RuntimeException("User is already a member of this group");
        }

        // Check if user account is active
        if (!user.getIsActive()) {
            throw new RuntimeException("Cannot add inactive user to group");
        }

        // Set the group relationship on the user (this updates the foreign key in users
        // table)
        user.setGroup(group);

        // Save the user - this will update the group_id foreign key in the database
        userRepository.save(user);

        // Refresh the group to get updated member count with members loaded
        groupRepository.flush();
        group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        // Trigger lazy loading
        if (group.getSupervisor() != null) {
            group.getSupervisor().getId();
        }
        group.getMembers().size();

        // Update memberIdsJson column
        group.updateMemberIdsJson();
        groupRepository.save(group);

        return group;
    }

    @Transactional
    public Group removeMemberFromGroup(Long groupId, Long userId) {
        User currentUser = authService.getCurrentUser();

        // Only FYP_COMMITTEE can remove members
        if (!currentUser.getRole().getName().equals("FYP_COMMITTEE")) {
            throw new RuntimeException("Only FYP Committee members can remove members from groups");
        }

        // Fetch and validate group
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        // Fetch and validate user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify user is in this group
        if (user.getGroup() == null || !user.getGroup().getId().equals(groupId)) {
            throw new RuntimeException("User is not a member of this group");
        }

        // Remove the group relationship (set foreign key to null)
        user.setGroup(null);

        // Save the user - this will set group_id to null in the database
        userRepository.save(user);

        // Refresh the group to get updated member count with members loaded
        groupRepository.flush();
        group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        // Trigger lazy loading
        if (group.getSupervisor() != null) {
            group.getSupervisor().getId();
        }
        group.getMembers().size();

        // Update memberIdsJson column
        group.updateMemberIdsJson();
        groupRepository.save(group);

        return group;
    }

    public List<User> getGroupMembers(Long groupId) {
        User currentUser = authService.getCurrentUser();
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        // Check access permissions
        if (!canUserAccessGroup(currentUser, group)) {
            throw new RuntimeException("You don't have permission to view this group's members");
        }

        return userRepository.findByGroupId(groupId);
    }

    /**
     * Assigns a supervisor to a group
     * 
     * @param group        The group to assign supervisor to
     * @param supervisorId The ID of the supervisor user
     */
    @Transactional
    public Group assignSupervisorToGroup(Long groupId, Long supervisorId) {
        User currentUser = authService.getCurrentUser();

        // Only FYP_COMMITTEE can assign supervisors
        if (!currentUser.getRole().getName().equals("FYP_COMMITTEE")) {
            throw new RuntimeException("Only FYP Committee members can assign supervisors to groups");
        }

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        assignSupervisorToGroup(group, supervisorId);

        groupRepository.save(group);
        groupRepository.flush();

        // Return group with supervisor and members loaded
        Group savedGroup = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        // Trigger lazy loading
        if (savedGroup.getSupervisor() != null) {
            savedGroup.getSupervisor().getId();
        }
        savedGroup.getMembers().size();

        return savedGroup;
    }

    /**
     * Internal method to assign supervisor to a group
     */
    private void assignSupervisorToGroup(Group group, Long supervisorId) {
        User supervisor = userRepository.findById(supervisorId)
                .orElseThrow(() -> new RuntimeException("Supervisor not found"));

        // Verify user is a supervisor
        if (!supervisor.getRole().getName().equals("SUPERVISOR")) {
            throw new RuntimeException("User is not a supervisor. User role: " + supervisor.getRole().getName());
        }

        // Check if supervisor account is active
        if (!supervisor.getIsActive()) {
            throw new RuntimeException("Cannot assign inactive supervisor to group");
        }

        group.setSupervisor(supervisor);
    }

    /**
     * Removes supervisor from a group
     */
    @Transactional
    public Group removeSupervisorFromGroup(Long groupId) {
        User currentUser = authService.getCurrentUser();

        // Only FYP_COMMITTEE can remove supervisors
        if (!currentUser.getRole().getName().equals("FYP_COMMITTEE")) {
            throw new RuntimeException("Only FYP Committee members can remove supervisors from groups");
        }

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        group.setSupervisor(null);

        groupRepository.save(group);
        groupRepository.flush();

        // Return group with supervisor and members loaded
        Group savedGroup = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        // Trigger lazy loading
        if (savedGroup.getSupervisor() != null) {
            savedGroup.getSupervisor().getId();
        }
        savedGroup.getMembers().size();

        return savedGroup;
    }

    private boolean canUserAccessGroup(User user, Group group) {
        String roleName = user.getRole().getName();

        // FYP_COMMITTEE and COMMITTEE_MEMBER can access all groups
        if (roleName.equals("FYP_COMMITTEE") || roleName.equals("COMMITTEE_MEMBER")) {
            return true;
        }

        // SUPERVISOR can access their supervised groups
        if (roleName.equals("SUPERVISOR") && group.getSupervisor() != null) {
            return group.getSupervisor().getId().equals(user.getId());
        }

        // STUDENT can access their own group
        if (roleName.equals("STUDENT") && user.getGroup() != null) {
            return user.getGroup().getId().equals(group.getId());
        }

        return false;
    }
}
