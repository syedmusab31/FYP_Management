package com.university.fyp.dto;

import com.university.fyp.entity.Group;
import com.university.fyp.entity.User;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class GroupResponse {
    private Long id;
    private String groupName;
    private String projectTitle;
    private String projectDescription;
    private Instant createdAt;

    // Supervisor information
    private SupervisorInfo supervisor;

    // Member IDs and information
    private List<Long> memberIds;
    private List<MemberInfo> members;
    private Integer memberCount;

    @Data
    public static class SupervisorInfo {
        private Long id;
        private String fullName;
        private String email;

        public SupervisorInfo(User user) {
            this.id = user.getId();
            this.fullName = user.getFullName();
            this.email = user.getEmail();
        }
    }

    @Data
    public static class MemberInfo {
        private Long id;
        private String fullName;
        private String email;
        private Boolean isActive;

        public MemberInfo(User user) {
            this.id = user.getId();
            this.fullName = user.getFullName();
            this.email = user.getEmail();
            this.isActive = user.getIsActive();
        }
    }

    public static GroupResponse fromGroup(Group group) {
        try {
            GroupResponse response = new GroupResponse();
            response.setId(group.getId());
            response.setGroupName(group.getGroupName());
            response.setProjectTitle(group.getProjectTitle());
            response.setProjectDescription(group.getProjectDescription());
            response.setCreatedAt(group.getCreatedAt());

            // Supervisor information
            try {
                if (group.getSupervisor() != null) {
                    response.setSupervisor(new SupervisorInfo(group.getSupervisor()));
                }
            } catch (Exception e) {
                System.err.println("Error loading supervisor for group " + group.getId() + ": " + e.getMessage());
                response.setSupervisor(null);
            }

            // Member information
            try {
                Set<User> memberSet = group.getMembers();
                if (memberSet != null && !memberSet.isEmpty()) {
                    List<Long> ids = memberSet.stream()
                            .map(User::getId)
                            .collect(Collectors.toList());
                    response.setMemberIds(ids);
                    response.setMemberCount(ids.size());

                    List<MemberInfo> memberInfos = memberSet.stream()
                            .map(user -> {
                                try {
                                    return new MemberInfo(user);
                                } catch (Exception e) {
                                    System.err.println("Error creating MemberInfo for user " + user.getId() + ": "
                                            + e.getMessage());
                                    return null;
                                }
                            })
                            .filter(info -> info != null)
                            .collect(Collectors.toList());
                    response.setMembers(memberInfos);
                } else {
                    response.setMemberIds(List.of());
                    response.setMembers(List.of());
                    response.setMemberCount(0);
                }
            } catch (Exception e) {
                System.err.println("Error loading members for group " + group.getId() + ": " + e.getMessage());
                e.printStackTrace();
                response.setMemberIds(List.of());
                response.setMembers(List.of());
                response.setMemberCount(0);
            }

            return response;
        } catch (Exception e) {
            System.err.println("Error converting group to response: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to convert group to response", e);
        }
    }
}
