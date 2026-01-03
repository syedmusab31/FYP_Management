package com.university.fyp.dto;

import com.university.fyp.entity.User;
import lombok.Data;

import java.time.Instant;

@Data
public class UserDTO {
    private Long id;
    private String email;
    private String fullName;
    private String roleName;
    private Long roleId;
    private Long groupId;
    private String groupName;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;

    public static UserDTO fromUser(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        
        if (user.getRole() != null) {
            dto.setRoleId(user.getRole().getId());
            dto.setRoleName(user.getRole().getName());
        }
        
        if (user.getGroup() != null) {
            dto.setGroupId(user.getGroup().getId());
            dto.setGroupName(user.getGroup().getGroupName());
        }
        
        dto.setIsActive(user.getIsActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        
        return dto;
    }
}

